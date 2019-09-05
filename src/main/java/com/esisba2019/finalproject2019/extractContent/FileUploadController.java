package com.esisba2019.finalproject2019.extractContent;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController {
  public static String uploadDirectory = System.getProperty("user.dir")+"/uploads/";


    @RequestMapping("/classification")
  public String upload(Model model, @RequestParam("files") MultipartFile[] files) throws IOException {
	  StringBuilder fileNames = new StringBuilder();
	  for (MultipartFile file : files) {
		  Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
		  System.out.println(uploadDirectory);
		  fileNames.append(file.getOriginalFilename()+" ");
          FileWriter w = new FileWriter("Links.txt",true);
          w.write(fileNameAndPath + "\n");
          w.close();
		  			Files.write(fileNameAndPath, file.getBytes());

	  }
	  model.addAttribute("msg", "Successfully uploaded files "+fileNames.toString());
	  return "classification2";
  }

}
