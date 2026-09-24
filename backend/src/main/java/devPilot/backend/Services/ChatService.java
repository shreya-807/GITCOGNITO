package devPilot.backend.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import devPilot.backend.Exceptions.BadRequestException;
import devPilot.backend.Exceptions.NotFoundException;
import devPilot.backend.Services.ai.ChatPromptBuilder;
import devPilot.backend.Services.ai.ChatStreamHandler;
import devPilot.backend.Services.ai.CitationMapper;
import devPilot.backend.Services.ai.CodeContextRetriever;
import devPilot.backend.Services.ai.RetrievedContext;
import devPilot.backend.Services.github.RepoService;
import devPilot.backend.dto.ChatMessageResponse;
import devPilot.backend.dto.ChatSessionResponse;
import devPilot.backend.dto.CreateChatSessionRequest;
import devPilot.backend.entity.ChatMessage;
import devPilot.backend.entity.ChatSession;
import devPilot.backend.entity.IndexStatus;
import devPilot.backend.entity.MessageRole;
import devPilot.backend.entity.Repository;
import devPilot.backend.repository.ChatMessageRepository;
import devPilot.backend.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RepoService repoService;
    private final CodeContextRetriever codeContextRetriever;
    private final ChatPromptBuilder chatPromptBuilder;
    private final ChatStreamHandler chatStreamHandler;
    private final CitationMapper citationMapper;

    @Transactional
    public ChatSessionResponse createSession(UUID userId, CreateChatSessionRequest request) {
        if (request == null || request.repositoryId() == null) {
            throw new BadRequestException("Repository id is required");
        }

        Repository repo = repoService.requireOwned(request.repositoryId(), userId);
        if (repo.getIndexStatus() != IndexStatus.READY) {
            throw new BadRequestException("Repository must be indexed before chatting");
        }

        String title = request.title() != null && !request.title().isBlank()
                ? request.title()
                : "Chat with " + repo.getFullName();

        ChatSession session = ChatSession.builder()
                .userId(userId)
                .repositoryId(repo.getId())
                .title(title)
                .build();

        session = chatSessionRepository.save(session);
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(UUID userId, UUID sessionId) {
        ChatSession session = requireSession(userId, sessionId);

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatSession requireSession(UUID userId, UUID sessionId) {
        return chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Chat session not found"));
    }

    @Transactional
    public SseEmitter streamReply(UUID userId, UUID sessionId, String userContent) {
        if (userContent == null || userContent.isBlank()) {
            throw new BadRequestException("Message content is required");
        }

        ChatSession session = requireSession(userId, sessionId);
        Repository repo = repoService.requireOwned(session.getRepositoryId(), userId);
        if (repo.getIndexStatus() != IndexStatus.READY) {
            throw new BadRequestException("Repository is not indexed");
        }

        ChatMessage userMessage = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(session.getId())
                .role(MessageRole.USER)
                .content(userContent)
                .build());

        RetrievedContext retrievedContext = codeContextRetriever.retrieve(repo.getId(), userContent);
        String systemPrompt = chatPromptBuilder.systemPrompt(repo.getFullName());
        String userPrompt = chatPromptBuilder.userPrompt(retrievedContext.contextText(), userContent);

        return chatStreamHandler.stream(
                session.getId(),
                toMessageResponse(userMessage),
                retrievedContext.citations(),
                systemPrompt,
                userPrompt);
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getRepositoryId(),
                session.getTitle(),
                session.getCreatedAt());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCitations() == null
                        ? List.of()
                        : citationMapper.fromJson(message.getCitations()),
                message.getCreatedAt());
    }

    public List<ChatSessionResponse> listSessions(UUID userId, UUID repositoryId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listSessions'");
    }
}

