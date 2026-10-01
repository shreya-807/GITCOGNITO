// package devPilot.backend.Security;
// import devPilot.backend.Services.UserService;
// import devPilot.backend.entity.User;

// import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.stereotype.Service;

// @Service
// public class GitHubOAuth2UserService
//         implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

//     private final UserService userService;

//     private final DefaultOAuth2UserService delegate =
//             new DefaultOAuth2UserService();

//     public GitHubOAuth2UserService(UserService userService) {
//         this.userService = userService;
//     }

//     @Override
//     public OAuth2User loadUser(OAuth2UserRequest userRequest)
//             throws OAuth2AuthenticationException {

//         OAuth2User githubUser =
//                 delegate.loadUser(userRequest);

//         String accessToken =
//                 userRequest.getAccessToken()
//                         .getTokenValue();

//         String scopes = "";

//         if (userRequest.getAccessToken().getScopes() != null) {
//             scopes = String.join(
//                     " ",
//                     userRequest.getAccessToken().getScopes()
//             );
//         }

//         User user = userService.upsertFromGitHub(
//                 githubUser.getAttributes(),
//                 accessToken,
//                 scopes
//         );

//         return new AppUserPrincipal(
//                 user,
//                 githubUser.getAttributes()
//         );
//     }
// }



package devPilot.backend.Security;

import devPilot.backend.Services.UserService;
import devPilot.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GitHubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(GitHubOAuth2UserService.class);

    private final UserService userService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public GitHubOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("Initiating OAuth2 user load from GitHub...");
            
            // Delegate loading the user from GitHub's user info endpoint
            OAuth2User githubUser = delegate.loadUser(userRequest);

            // Extract access token value
            String accessToken = userRequest.getAccessToken().getTokenValue();

            // Extract granted scopes safely
            String scopes = "";
            if (userRequest.getAccessToken().getScopes() != null) {
                scopes = String.join(" ", userRequest.getAccessToken().getScopes());
            }

            log.debug("Successfully fetched GitHub profile. Upserting user into database...");

            // Save or update user in your PostgreSQL database
            User user = userService.upsertFromGitHub(
                    githubUser.getAttributes(),
                    accessToken,
                    scopes
            );

            log.info("User successfully authenticated and upserted: {}", user.getId());

            // Return the custom principal structure
            return new AppUserPrincipal(
                    user,
                    githubUser.getAttributes()
            );

        } catch (Exception ex) {
            log.error("Error occurred while processing GitHub OAuth2 user login", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }
}