package hu.elte.alkfejl.issuetracker.api;

import hu.elte.alkfejl.issuetracker.model.Issue;
import hu.elte.alkfejl.issuetracker.model.IssueMessage;
import hu.elte.alkfejl.issuetracker.service.IssueService;
import hu.elte.alkfejl.issuetracker.service.UserService;
import hu.elte.alkfejl.issuetracker.service.annotations.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static hu.elte.alkfejl.issuetracker.model.User.Role.ADMIN;
import static hu.elte.alkfejl.issuetracker.model.User.Role.USER;

/**
 * @author Godzsák Dávid <godzsakdavid@gmail.com>
 */
@RestController
@RequestMapping("/api/issues")
public class IssueApiController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private UserService userService;

    @Role({ADMIN, USER})
    @GetMapping
    private ResponseEntity<Iterable<Issue>> list() {
        Iterable<Issue> issues = issueService.listByRole(userService.getUser());
        return ResponseEntity.ok(issues);
    }

    @Role({ADMIN, USER})
    @PostMapping
    private ResponseEntity<Issue> create(@RequestBody Issue issue) {
        issue.setUser(userService.getUserRepository().findByUsername(userService.getUser().getUsername()).get());
        Issue saved = issueService.create(issue);
        return ResponseEntity.ok(saved);
    }

    @Role({ADMIN, USER})
    @GetMapping("/{id}")
    private ResponseEntity<Issue> read(@PathVariable String id) {
        Issue read = issueService.read(Integer.parseInt(id));
        return ResponseEntity.ok(read);
    }

    @Role(ADMIN)
    @PutMapping("/{id}")
    private ResponseEntity<Issue> update(@PathVariable int id, @RequestBody Issue issue) {
        Issue updated = issueService.update(id, issue);
        return ResponseEntity.ok(updated);
    }

    @Role(ADMIN)
    @DeleteMapping("/{id}")
    private ResponseEntity delete(@PathVariable int id) {
        issueService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Role({ADMIN, USER})
    @GetMapping("/{id}/messages")
    private ResponseEntity<Iterable<IssueMessage>> getMessages(@PathVariable int id) {
        Issue read = issueService.read(id);
        return ResponseEntity.ok(read.getMessages());
    }

    @Role({ADMIN, USER})
    @GetMapping("/{id}/messages/{mess_id}")
    private ResponseEntity<IssueMessage> getMessage(@PathVariable int id, @PathVariable int mess_id) {
        Issue read = issueService.read(id);
        IssueMessage mess = read.getMessages().stream().filter(message -> message.getId() == mess_id).findFirst().orElse(null);
        if (mess == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mess);
        }
    }

    @Role({ADMIN, USER})
    @PostMapping("/{id}/messages")
    private ResponseEntity<Issue> createMessage(@PathVariable int id, @RequestBody IssueMessage mess) {
        mess.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        Issue read = issueService.read(id);
        List<IssueMessage> messages = read.getMessages();
        messages.add(mess);
        read.setMessages(messages);
        Issue saved = issueService.update(id, read);
        return ResponseEntity.ok(saved);
    }

    @Role({ADMIN, USER})
    @DeleteMapping("/{id}/messages")
    private ResponseEntity<Issue> deleteMessages(@PathVariable int id) {
        Issue read = issueService.read(id);
        read.getMessages().clear();
        Issue saved = issueService.update(id, read);
        return ResponseEntity.ok(saved);
    }

    @Role({ADMIN, USER})
    @DeleteMapping("/{id}/messages/{mess_id}")
    private ResponseEntity<Issue> deleteMessage(@PathVariable int id, @PathVariable int mess_id) {
        Issue read = issueService.read(id);
        IssueMessage messageToRemove = read.getMessages().stream().filter(mess -> mess.getId() == mess_id).findFirst().orElse(null);
        read.getMessages().remove(messageToRemove);
        Issue saved = issueService.update(id, read);
        return ResponseEntity.ok(saved);
    }
}
