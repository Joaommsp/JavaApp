package br.com.joaomarcos.todolist.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaomarcos.utils.Utils;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaksRepository taksRepository;
  
  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("A data de início/ data de término deve ser maior que adata Atual");
    }

     if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("A data de início deve vir antes da data de término");
    }

    var task = this.taksRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var tasks = this.taksRepository.findByIdUser((UUID) idUser);

    return tasks;
  }

  // http://localhpst:8080/tasks/293919-290921b2230-328901
  @PutMapping("/{id}")
  public ResponseEntity Update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taksRepository.findById(id).orElse(null);

    if(task == null) {
       return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("Esta tarefa não encontrada");
    }

    var idUser = request.getAttribute("idUser");

    if(!task.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("Esta tarefa não pertence ao seu uruário");
    
    }

    Utils.copyNonNullProperties(taskModel, task);
    var taskUpdatade = this.taksRepository.save(task);
    return ResponseEntity.ok().body(taskUpdatade);
  }

}
