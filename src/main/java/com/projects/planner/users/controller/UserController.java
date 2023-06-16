package com.projects.planner.users.controller;

import com.projects.planner.entity.User;
import com.projects.planner.users.dto.UserSearchDto;
import com.projects.planner.users.service.UserService;
import com.projects.planner.utils.Checker;
import com.projects.planner.utils.webclient.UserWebClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    public static final String ID_COLUMN = "id";
    private final UserService userService;
    private final UserWebClientBuilder userWebClientBuilder;

    @PostMapping("/add")
    public ResponseEntity<User> add(@RequestBody User user) {

        try {
            Checker.idNotNull(user.getId());
            Checker.paramIsNullOrEmpty(user.getEmail(), "EMAIL");
            Checker.paramIsNullOrEmpty(user.getUsername(), "USERNAME");
            Checker.paramIsNullOrEmpty(user.getPassword(), "PASSWORD");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }

        user = userService.add(user);

        if (user != null) {
            userWebClientBuilder.setDefaultUserData(user.getId()).subscribe(
                    result -> System.out.println("Default User Data " + (result ? "is Created" : "not Created"))
            );
        }

        return ResponseEntity.ok(user); // возвращаем созданный объект со сгенерированным id

    }


    // обновление
    @PutMapping("/update")
    public ResponseEntity<User> update(@RequestBody User user) {

        try {
            Checker.idIsNullOrZero(user.getId());
            Checker.paramIsNullOrEmpty(user.getEmail(), "EMAIL");
            Checker.paramIsNullOrEmpty(user.getUsername(), "USERNAME");
            Checker.paramIsNullOrEmpty(user.getPassword(), "PASSWORD");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }

        userService.update(user);

        return new ResponseEntity(HttpStatus.OK);

    }

    @PostMapping("/delete-by-id")
    public ResponseEntity deleteByUserId(@RequestBody Long userId) {

        try {
            Checker.idIsNullOrZero(userId);
            userService.deleteByUserId(userId);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("UserId = " + userId + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK);

    }

    @PostMapping("/delete-by-email")
    public ResponseEntity deleteByUserEmail(@RequestBody String email) {

        try {
            Checker.paramIsNullOrEmpty(email, "EMAIL");
            userService.deleteByUserEmail(email);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("EMAIL = " + email + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK);

    }

    @PostMapping("/id")
    public ResponseEntity<User> findById(@RequestBody Long id) {

        Optional<User> userOptional = userService.findById(id);;

        try {
            Checker.idIsNullOrZero(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        return new ResponseEntity("UserId = " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
    }

    @PostMapping("/email")
    public ResponseEntity<User> findByEmail(@RequestBody String email) {

        User user = null;

        try {
            Checker.paramIsNullOrEmpty(email, "EMAIL");
            user = userService.findByEmail(email);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("EMAIL = " + email + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<User>> search(@RequestBody UserSearchDto searchDto) throws ParseException {

        String email = searchDto.getEmail() != null ? searchDto.getEmail() : null;
        String username = searchDto.getUsername() != null ? searchDto.getUsername() : null;

        String sortColumn = searchDto.getSortColumn() != null ? searchDto.getSortColumn() : null;
        String sortDirection = searchDto.getSortDirection() != null ? searchDto.getSortDirection() : null;

        Integer pageNumber = searchDto.getPageNumber() != null ? searchDto.getPageNumber() : null;
        Integer pageSize = searchDto.getPageSize() != null ? searchDto.getPageSize() : null;

        // направление сортировки
        Sort.Direction direction = sortDirection == null || sortDirection.trim().length() == 0 || sortDirection.trim().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок.
            Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
            Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к. не указано второе поле сортировки.
            Поэтому и используем ID - тогда все записи с одинаковым значением приоритета будут следовать в одном порядке по ID.
         */

        // объект сортировки, который содержит стобец и направление
        Sort sort = Sort.by(direction, sortColumn, ID_COLUMN);

        // объект постраничности
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        // результат запроса с постраничным выводом
        Page<User> result = userService.findByParams(email, username, pageRequest);

        // результат запроса
        return ResponseEntity.ok(result);

    }

}
