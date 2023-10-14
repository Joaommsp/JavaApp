package br.com.joaomarcos.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.joaomarcos.todolist.user.IUserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var servletPath = request.getServletPath();

    if (servletPath.startsWith("/tasks/")) {
      // pegar a autenticação (user e senha)
      var authorization = request.getHeader("Authorization");

      var authEncoded = authorization.substring("Basic".length()).trim();

      byte[] authDecode = Base64.getDecoder().decode(authEncoded);

      var authString = new String(authDecode);
      // [user:senha]
      String[] credentials = authString.split(":");
      String username = credentials[0];
      String password = credentials[1];
      // System.out.println("Authorization");
      // System.out.println(username);
      // System.out.println(password);

      // Validar User
      var user = this.userRepository.findByUsername(username);
      if (user == null) {
        response.sendError(403);
      } else {
        // Validar Senha
        var passwordverify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (passwordverify.verified) {
          // Segue Viagem
          request.setAttribute("idUser",user.getId());
          filterChain.doFilter(request, response);
        } else {
          response.sendError(401);
        }

        // Segue viagem
      }
    } else {
      filterChain.doFilter(request, response);
    }

  }

}
