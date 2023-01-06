package com.ecom.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecom.beans.Admin;
import com.ecom.beans.Category;
import com.ecom.repository.AdminRepository;
import com.ecom.service.CategoryService;
import com.ecom.service.SubcategoryService;

@Controller
@RequestMapping("/catagory")
public class CatagoryController {
	@Autowired
	CategoryService catagoryservice;
	@Autowired
	SubcategoryService subcatagoryservice;
	@Autowired
	AdminRepository adminRepository;
	@RequestMapping("/catagorylist/{eid}")
	public String catagoryList(Model model, Category catagory, @PathVariable("eid") int employeeId,
			HttpServletRequest request) {
		HttpSession adminSession = request.getSession();
		if (adminSession.getAttribute("adminSession") != null) {
			Admin admin = adminRepository.getById(employeeId);
			model.addAttribute("admin", admin);
			List<Category> catagorylist = catagoryservice.getAllCategory();
			model.addAttribute("catagorylist", catagorylist);
			model.addAttribute("catagory", catagory);
			return "category";
		} else {
			return "redirect:/emp/back-end";
		}
	}

	@RequestMapping("/savecatagory/{eid}")
	public String saveCatagory(Category catagory, @PathVariable("eid") int employeeId, HttpServletRequest request) {
		HttpSession adminSession = request.getSession();
		if (adminSession.getAttribute("adminSession") != null) {
			catagoryservice.addCategory(catagory);
			return "redirect:/catagory/catagorylist/" + employeeId;
		} else {
			return "redirect:/emp/back-end";
		}
	}

	@GetMapping("/deleteCategory/{id}/{eid}")
	public String deleteCategory(Model model, @PathVariable("id") int catagoryId, @PathVariable("eid") int employeeId,
			HttpServletRequest request) {
		HttpSession adminSession = request.getSession();
		if (adminSession.getAttribute("adminSession") != null) {
			Admin admin = adminRepository.getById(employeeId);
			model.addAttribute("admin", admin);
			System.out.println("ssss");
			catagoryservice.deleteCategoryById(catagoryId);
			return "redirect:/catagory/catagorylist/" + employeeId;
		} else {
			return "redirect:/emp/back-end";
		}
	}

	@GetMapping("/editcategory/{id}/{eid}")
	public String editCategoryBycategoryid(Model model, @PathVariable("id") int catagoryId, @PathVariable("eid") int employeeId,
			HttpServletRequest request) {
		HttpSession adminSession = request.getSession();
		if (adminSession.getAttribute("adminSession") != null) {
		Admin admin = adminRepository.getById(employeeId);
		model.addAttribute("admin", admin);
		Category category = catagoryservice.getCategoryById(catagoryId);
		model.addAttribute("categorieslist",category);
		return "EditCategory";
		} else {
			return "redirect:/emp/back-end";
		}
	}

}
