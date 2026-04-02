// package ba.core.config;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.security.SignatureException;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.NonNull;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
// import java.io.IOException;
// import java.security.Key;
// import java.util.Collections;

// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     @Value("${security.jwt.secret-key}")
//     private String jwtSecret;

//     @Override
//     protected void doFilterInternal(
//             @NonNull HttpServletRequest request,
//             @NonNull HttpServletResponse response,
//             @NonNull FilterChain filterChain
//             ) throws ServletException, IOException {
//         final String authHeader = request.getHeader("Authorization");

//         if(authHeader == null || !authHeader.startsWith("Bearer")){
//             filterChain.doFilter(request, response);
//             return;
//         }

//         final String token = authHeader.substring(7);
//         final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

//         try {
//             Claims claims = Jwts
//                     .parserBuilder()
//                     .setSigningKey(key)
//                     .build()
//                     .parseClaimsJws(token)
//                     .getBody();

//             String subject = claims.getSubject();

//             if(subject != null && SecurityContextHolder.getContext().getAuthentication() == null){
//                 UsernamePasswordAuthenticationToken authToken =
//                         new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList());
//                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                 SecurityContextHolder.getContext().setAuthentication(authToken);
//             }

//         } catch (SignatureException e) {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("Invalid JWT: " + e.getMessage());
//         } catch (Exception e){
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("Authentication failed: " + e.getMessage());
//         }

//         filterChain.doFilter(request, response);
//     }
// }
