package com.example.login_administrator.controller;

import com.example.login_administrator.dto.UserUpdateDto;
import com.example.login_administrator.dto.UsersResponseDTO;
import com.example.login_administrator.model.User;
import com.example.login_administrator.service.AdminService;
import com.itextpdf.text.DocumentException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping(value = "/v1/admin")
public class AdminController {
    @Autowired
    AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> getAll(@RequestParam int page, @RequestParam int size,
                                         @RequestParam(required = false, defaultValue = "false") boolean sortByName) {
        return adminService.getAllUser(page, size, sortByName);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable Long id) {
        return adminService.updateUser(userUpdateDto, id);
    }

//    @GetMapping("/users/export/excel")
//    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
//        List<User> users = adminService.getAllUsers(0, Integer.MAX_VALUE, false);
//        ByteArrayInputStream in = adminService.exportToExcel(users);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Disposition", "attachment; filename=users.xlsx");
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(new InputStreamResource(in));
//    }

//    @GetMapping("/users/export/pdf")
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public ResponseEntity<String> exportToPdf() throws IOException, DocumentException {
//        List<UsersResponseDTO> users = adminService.getAllUsers(1, Integer.MAX_VALUE, false);
//        adminService.exportToPdf(users);
//
//        return ResponseEntity.ok("File exported to the configured directory");
//    }
}
