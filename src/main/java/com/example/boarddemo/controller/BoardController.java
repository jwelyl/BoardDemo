package com.example.boarddemo.controller;

import com.example.boarddemo.domain.dto.BoardDto;
import com.example.boarddemo.domain.dto.FileDto;
import com.example.boarddemo.service.BoardService;
import com.example.boarddemo.service.FileService;
import com.example.boarddemo.util.MD5Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final FileService fileService;

    @Value("${spring.servlet.multipart.location}")
    private String savePath;

    @GetMapping("/")
    public String list(Model model) {
        List<BoardDto> boardDtoList = boardService.getBoardList();
        model.addAttribute("postList", boardDtoList);
        return "board/list.html";
    }

    @GetMapping("/post")
    public String post() {
        return "board/post.html";
    }

    @PostMapping("/post")
    public String write(@RequestParam("file") MultipartFile files, BoardDto boardDto) {
       try {
           String origFilename = files.getOriginalFilename();
           String filename = new MD5Generator(origFilename).toString();

           /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
//           String savePath = System.getProperty("user.dir") + "\\files";
//           String savePath = "C:\\Users\\SSAFY\\Downloads\\files";
           System.out.println("savePath = " + savePath);

           /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
           if(!new File(savePath).exists()) {
               try {
                   new File(savePath).mkdir();
               }
               catch (Exception e) {
                   e.getStackTrace();
               }
           }

           String filePath = savePath + "\\" + filename + ".jpg";
           files.transferTo(new File(filePath));

//           FileDto fileDto = new FileDto();
//           fileDto.setOrigFilename(origFilename);
//           fileDto.setFilename(filename);
//           fileDto.setFilePath(filePath);
//
//           Long fileId = fileService.saveFile(fileDto);
//           boardDto.setFileId(fileId);
           boardDto.setFilePath(filePath);
           boardService.savePost(boardDto);
       } catch(Exception e) {
           e.printStackTrace();
       }

       return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        BoardDto boardDto = boardService.getPost(id);

        System.out.println("boardDto.getFilePath() = " + boardDto.getFilePath());
        model.addAttribute("post", boardDto);
        return "board/detail.html";
    }

    @GetMapping("/post/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        BoardDto boardDto = boardService.getPost(id);
        model.addAttribute("post", boardDto);
        return "board/edit.html";
    }

    @PutMapping("/post/edit/{id}")
    public String update(BoardDto boardDto) {
        boardService.savePost(boardDto);
        return "redirect:/";
    }

    @DeleteMapping("/post/{id}")
    public String delete(@PathVariable("id") Long id) {
        boardService.deletePost(id);
        return "redirect:/";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId")Long fileId) throws IOException {
        FileDto fileDto = fileService.getFile(fileId);
        Path path = Paths.get(fileDto.getFilePath());
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attatchment; filename=\"" + fileDto.getOrigFilename() + "\"")
                .body(resource);
    }
}
