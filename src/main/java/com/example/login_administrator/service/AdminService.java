package com.example.login_administrator.service;

import com.example.login_administrator.dto.UserDto;
import com.example.login_administrator.dto.UserUpdateDto;
import com.example.login_administrator.dto.UserWithPageDto;
import com.example.login_administrator.dto.UsersResponseDTO;
import com.example.login_administrator.model.Role;
import com.example.login_administrator.model.User;
import com.example.login_administrator.repository.UserRepository;
import com.example.login_administrator.utils.Response;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Value("${export.directory}")
    private String exportDirectory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    public List<UsersResponseDTO> getAllUsers(int page, int count, boolean sortByName) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    UsersResponseDTO dto = modelMapper.map(user, UsersResponseDTO.class);
                    dto.setRole(user.getRoles().stream()
                            .map(Role::getName).toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ResponseEntity<Object> getAllUser(int page, int count, boolean sortByName) {
        if(page<1){
            return Response.build("page must not be less than 1", null, null, HttpStatus.BAD_REQUEST);
        }
        if(count<1){
            return Response.build("count must not be less than 1", null, null, HttpStatus.BAD_REQUEST);
        }
        Pageable pageable = PageRequest.of(page-1, count);
        Page<User> userList;

        if (sortByName) {
            userList = userRepository.findAllByOrderByNameAsc(pageable);
        } else {
            userList = userRepository.findAll(pageable);
        }
        List<UsersResponseDTO> users = userList.getContent().stream()
                .map(user -> modelMapper.map(user, UsersResponseDTO.class))
                .collect(Collectors.toList());

        UserWithPageDto userWithPageDto = UserWithPageDto.builder()
                .users(users)
                .page(page)
                .pageAvailable(userList.getTotalPages())
                .build();

        return Response.build(Response.get("user data"), userWithPageDto, null, HttpStatus.OK);
    }

    public ResponseEntity<Object> updateUser(UserUpdateDto userUpdateDto, Long id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            return Response.build(Response.notFound("User"), null, null, HttpStatus.BAD_REQUEST);
        }

        user.get().setName(userUpdateDto.getName());
        user.get().setEmail(userUpdateDto.getEmail());
        user.get().setPhone(userUpdateDto.getPhone());

        if(!user.get().getPhone().equals(userUpdateDto.getPhone())){
            if (Boolean.TRUE.equals(userRepository.existsByPhone(userUpdateDto.getPhone()))) {
                return Response.build(Response.exist("User", "phone", userUpdateDto.getPhone()), null, null, HttpStatus.BAD_REQUEST);
            }
            user.get().setPhone(userUpdateDto.getPhone());
        }

        userRepository.save(user.get());

        UserDto userDto = UserDto.builder()
                .id(user.get().getId())
                .name(user.get().getName())
                .email(user.get().getEmail())
                .phone(user.get().getPhone())
                .updatedAt(user.get().getUpdatedAt())
                .build();

        return Response.build(Response.update("user data"), userDto, null, HttpStatus.CREATED);
    }

    public void exportToPdf(List<UsersResponseDTO> users) throws IOException, DocumentException {
        Document document = new Document();
        try (FileOutputStream fileOut = new FileOutputStream(exportDirectory + "/users.pdf")) {
            PdfWriter.getInstance(document, fileOut);
            document.open();
            document.add(new Paragraph("Users"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            table.addCell("ID");
//            table.addCell("Name");
//            table.addCell("Email");
//            table.addCell("Password");

            // Add table data
            for (UsersResponseDTO user : users) {
                table.addCell(String.valueOf(user.getId()));
//                table.addCell(user.getName());
//                table.addCell(user.getEmail());
//                table.addCell(user.getPassword());
            }

            document.add(table);
        } catch (DocumentException | IOException e) {
            // Handle exceptions here if needed
            e.printStackTrace();
        }
    }

    public void exportToExcel(List<UsersResponseDTO> users) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Name", "Email"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data Rows
        int rowNum = 1;
        for (UsersResponseDTO user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(2).setCellValue(user.getRole().toString());
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(exportDirectory + "/users.xlsx")) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}
