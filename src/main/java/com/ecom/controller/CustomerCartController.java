package com.ecom.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import com.ecom.beans.Customer;
import com.ecom.beans.CustomerCart;
import com.ecom.beans.DigitalProducts;
import com.ecom.beans.OrderByCustomer;
import com.ecom.beans.PhysicalCategory;
import com.ecom.beans.PhysicalProducts;
import com.ecom.beans.PhysicalSubCategory;
import com.ecom.repository.CustomerCartRepository;
import com.ecom.repository.CustomerOrderRepository;
import com.ecom.repository.DigitalProductsRepository;
import com.ecom.repository.OrderByCustomerRepository;
import com.ecom.repository.PhysicalProductRepository;
import com.ecom.service.CustomerCartService;
import com.ecom.service.CustomerService;
import com.ecom.service.PhysicalCategoryService;
import com.ecom.service.PhysicalProductService;
import com.ecom.service.PhysicalSubCategoryService;
import com.ecom.service.SubcategoryService;

@Controller
@RequestMapping("/cart")
public class CustomerCartController {

	@Autowired
	PhysicalCategoryService PhysicalCategoryService;
	@Autowired
	PhysicalProductService physicalProductService;
	@Autowired
	CustomerOrderRepository customerOrderRepository;
	@Autowired
	CustomerCartService customerCartService;
	@Autowired
	CustomerService customerservice;
	@Autowired
	PhysicalCategoryService PhysicalCategoryServices;
	@Autowired
	SubcategoryService subcatagoryservice;
	@Autowired
	PhysicalProductService physicalproductservice;
	@Autowired
	PhysicalSubCategoryService PhysicalSubCategoryServices;
	@Autowired
	PhysicalProductRepository physicalProductRepository;
	@Autowired
	CustomerCartRepository customerCartRepository;
	@Autowired
	DigitalProductsRepository digitalProductsRepository;
	@Autowired
	OrderByCustomerRepository orderByCustomerRepository;

	@RequestMapping("/cartloginpage")
	public String cartloginpage(Model model, @ModelAttribute(value = "customerObject") Customer customerObject) {
		model.addAttribute("customerObject", customerObject);
		return "front-end-Cartlogin";
	}

	@RequestMapping("/frontendcart")
	public String frontendcart(Model model, @ModelAttribute(value = "customerObject") Customer customerObject) {
		model.addAttribute("customerObject", customerObject);
		return "Cartlogin";
	}

	@RequestMapping("/Cartregistration")
	public String Cartregistration(Model model, @ModelAttribute(value = "customerObject") Customer customerObject,
			@RequestParam(required = false) String message) {
		if (!StringUtils.isEmpty(message)) {
			model.addAttribute("msg", message);
		}
		model.addAttribute("customerObject", customerObject);
		return "front-end-Cartregister";
	}

	@RequestMapping(value = "/savecartCustomer", method = RequestMethod.POST)
	public String savecartCustomer(Model model, @ModelAttribute(value = "customerObject") Customer customerObject,
			RedirectAttributes redirectAttributes) {
		if (customerObject.getConfirmPassword().equals(customerObject.getPassword())) {
			customerObject.setIsActive('Y');
			Customer customer = customerservice.addCustomer(customerObject);
			customer.setCreatedBy(customer.getCustomerId());
			customer.setUpdatedBy(customer.getCustomerId());
			customerservice.addCustomer(customer);
			return "redirect:/cart/frontendcart";
		} else {
			redirectAttributes.addFlashAttribute("message", "password and confirm password should be same ");

			return "rediect:/cart/Cartregistration";
		}
	}

	@GetMapping("/customercartlogin")
	public String customercartlogin(Model model, @ModelAttribute(value = "customerObject") Customer customerObject,
			HttpServletRequest request) {
		Customer signinObj = customerservice.getCustomer(customerObject.getEmail(), customerObject.getPassword());
		if (signinObj != null) {
			signinObj.setCreated(LocalDate.now());
			signinObj.setUpdated(LocalDate.now());
			Customer customer = customerservice.addCustomer(signinObj);
			return "redirect:/cart/frontendproducts/" + customer.getCustomerId();
		}

		else {
			Customer cObject = new Customer();
			model.addAttribute("customerObject", cObject);
			model.addAttribute("msg", "The entered details are wrong.\t Please check your Email and password");
			return "redirect:/cart/frontendcart";
		}
	}

	@RequestMapping("/frontendproducts/{id}")
	public String frontendproductsByCustomerId(Model model, @PathVariable(value = "id") int customerId,
			@RequestParam(required = false) String message) {
		if (!StringUtils.isEmpty(message)) {
			model.addAttribute("message", message);
		}
		Customer object = customerservice.getCustomerById(customerId);
		List<CustomerCart> cartlist = customerCartRepository.getBillOrderList(object.getCustomerId());
		for (CustomerCart cart : cartlist) {
			PhysicalProducts product = physicalProductService.getProductById(cart.getProductId());
			List<PhysicalProducts> productwithisactivey = physicalProductRepository
					.getPhysicalProductsForadding(product.getProductModelNumber());
			if (productwithisactivey.size() != 0) {
				for (PhysicalProducts producty : productwithisactivey) {
					producty.setQuantity(producty.getQuantity() - 1);
					if (producty.getQuantity() == 0) {
						producty.setAvalablity("OUT OF STOCK");
					} else {
						producty.setAvalablity("IN STOCK");
					}
					physicalProductService.addProduct(producty);
				}
			}
			if (product.getIsactive() != 'Y') {
				product.setIsactive('S');
				physicalProductService.addProduct(product);
			}
			cart.setIsActive('O');
			customerCartService.addCart(cart);
		}
		List<OrderByCustomer> orderByCustomers = orderByCustomerRepository.placedOrder(customerId);
		for (OrderByCustomer order : orderByCustomers) {
			order.setIsActive('B');
			orderByCustomerRepository.save(order);
		}
		model.addAttribute("customer", object);
		List<PhysicalProducts> products = physicalProductRepository.findLatestProducts();
		model.addAttribute("product", products);
		List<PhysicalCategory> catagorylist = PhysicalCategoryServices.getAllCategory();
		model.addAttribute("catagorylist", catagorylist);
		List<PhysicalSubCategory> subcatagorylist = PhysicalSubCategoryServices.list();
		model.addAttribute("subcatagorylist", subcatagorylist);
		List<PhysicalProducts> productobject = physicalProductRepository.getActivePhysicalProducts();
		model.addAttribute("productlist", productobject);
		List<CustomerCart> list = customerCartRepository.getAllCartList(customerId);
		model.addAttribute("addQuatity", list.size());
		List<DigitalProducts> digitalProductsList = digitalProductsRepository.findAll();
		model.addAttribute("digitalProductsList", digitalProductsList);
		return "front-end-products";
	}

	@RequestMapping("/addtocart/{id}/{cid}")
	public String addtocart(Model model, @PathVariable(value = "id") int productId, CustomerCart cart,
			@PathVariable(value = "cid") int customerId,
			@RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity) {
		PhysicalProducts product = physicalProductService.getProductById(productId);
		List<PhysicalProducts> physicalProducts = physicalProductRepository
				.getPhysicalProductsByModelNumber(product.getProductModelNumber());
		List<PhysicalProducts> productlistforquatity = physicalProductRepository
				.getPhysicalProductsByModelNumberForQuantity(product.getProductModelNumber());
		Customer customer = customerservice.getCustomerById(customerId);

		List<CustomerCart> list = customerCartRepository.getCartActiveList(customer.getCustomerId());
		List<CustomerCart> customerCartlist = customerCartRepository.getAllcartListforproduct(customerId,
				product.getProductModelNumber());
		ArrayList<String> modelNumbers = new ArrayList<>();
		for (CustomerCart c : list) {
			modelNumbers.add(c.getProductModelNumber());

		}
		ArrayList<Integer> cartIds = new ArrayList<>();
		for (CustomerCart c : customerCartlist) {
			cartIds.add(c.getProductId());
		}
		int count = 0;
		for (PhysicalProducts products : physicalProducts) {
			if (cartIds.contains(products.getProductId())) {
				count++;
			}
		}
		if (customerCartlist.size() < productlistforquatity.size() && product.getQuantity() > 0) {
			if (modelNumbers.contains(product.getProductModelNumber())) {
				if (physicalProducts.size() > count) {
					for (PhysicalProducts products : physicalProducts) {
						if (cartIds.contains(products.getProductId())) {
							continue;
						} else {
							CustomerCart data = customerCartRepository.addQuantityofProduct(customerId,
									product.getProductModelNumber());
							data.setQuantity(data.getQuantity() + 1);
							data.setTotalprice(data.getProductPrice() * (data.getQuantity()));
							customerCartRepository.save(data);
							cart.setImage(products.getProductImage());
							cart.setCustomerId(customerId);
							cart.setProductId(products.getProductId());
							cart.setProductName(products.getProductName());
							cart.setProductModelNumber(products.getProductModelNumber());
							cart.setProductPrice(products.getProductMRPPrice());
							cart.setProductCompany(products.getProductCompany());
							cart.setProductCode(products.getProductCode());
							cart.setStore(products.getStoreName());
							cart.setIsActive('N');
							cart.setQuantity(1);
							customerCartService.addCart(cart);
							break;
						}
					}
				} else {
					CustomerCart data = customerCartRepository.addQuantityofProduct(customerId,
							product.getProductModelNumber());
					data.setQuantity(data.getQuantity() + 1);
					data.setTotalprice(data.getProductPrice() * (data.getQuantity()));
					customerCartRepository.save(data);
					cart.setImage(product.getProductImage());
					cart.setCustomerId(customerId);
					cart.setProductId(product.getProductId());
					cart.setStore(product.getStoreName());
					cart.setProductName(product.getProductName());
					cart.setProductModelNumber(product.getProductModelNumber());
					cart.setProductPrice(product.getProductMRPPrice());
					cart.setProductCompany(product.getProductCompany());
					cart.setProductCode(product.getProductCode());
					cart.setQuantity(1);
					cart.setIsActive('N');
					customerCartService.addCart(cart);
				}
			} else {
				if (physicalProducts.size() != 0) {
					for (PhysicalProducts products : physicalProducts) {
						cart.setImage(products.getProductImage());
						cart.setStore(products.getStoreName());
						cart.setCustomerId(customerId);
						cart.setProductId(products.getProductId());
						cart.setProductName(products.getProductName());
						cart.setProductModelNumber(products.getProductModelNumber());
						cart.setProductPrice(products.getProductMRPPrice());
						cart.setProductCompany(products.getProductCompany());
						cart.setProductCode(products.getProductCode());
						cart.setIsActive('Y');
						cart.setTotalprice(products.getProductMRPPrice());
						cart.setQuantity(1);
						customerCartService.addCart(cart);
						break;
					}
				} else {
					cart.setImage(product.getProductImage());
					cart.setCustomerId(customerId);
					cart.setProductId(product.getProductId());
					cart.setProductName(product.getProductName());
					cart.setProductModelNumber(product.getProductModelNumber());
					cart.setProductPrice(product.getProductMRPPrice());
					cart.setProductCompany(product.getProductCompany());
					cart.setProductCode(product.getProductCode());
					cart.setStore(product.getStoreName());
					cart.setIsActive('Y');
					cart.setTotalprice(product.getProductMRPPrice());
					customerCartService.addCart(cart);
				}
			}
			return "redirect:/cart/frontendproducts/" + customerId;
		} else {
			return "redirect:/cart/frontendproducts/" + customerId;
		}
	}

	@RequestMapping("/cartList/{cid}")
	public String cartList(Model model, CustomerCart cart, @PathVariable("cid") int customerId,
			@RequestParam(required = false) String message) {
		if (!StringUtils.isEmpty(message)) {
			model.addAttribute("message", message);
		}
		float totalcartprice = 0;
		List<CustomerCart> list = customerCartRepository.getCartActiveList(customerId);
		Customer data = customerservice.getCustomerById(customerId);
		if (customerCartRepository.getcarttotal(customerId) != null) {
			totalcartprice = Float.parseFloat(customerCartRepository.getcarttotal(customerId));
		} else {
			totalcartprice = 0;
		}
		model.addAttribute("totalcartprice", totalcartprice);
		model.addAttribute("Cart", list);
		model.addAttribute("customer", data);
		return "cart";
	}

	@RequestMapping("/deleteCart/{id}/{cid}")
	public String deleteCart(Model model, @PathVariable("id") int customerCartId, @PathVariable("cid") int cid,
			RedirectAttributes redirectAttributes) {
		CustomerCart cart = customerCartRepository.getById(customerCartId);
		List<CustomerCart> QuantityList = customerCartRepository.DeleteQuantityofProduct(cid,
				cart.getProductModelNumber());
		if (QuantityList.size() != 0) {
			for (CustomerCart cartProduct : QuantityList) {
				customerCartService.deleteBydataId(cartProduct.getCartId());
				break;
			}
			cart.setQuantity(cart.getQuantity() - 1);
			cart.setTotalprice(cart.getTotalprice() - cart.getProductPrice());
			customerCartRepository.save(cart);
		} else {
			customerCartRepository.deleteById(customerCartId);
		}
		redirectAttributes.addFlashAttribute("message", "Product has been delete from cart");

		return "redirect:/cart/cartList/" + cid;

	}

	@RequestMapping("/addquatity/{id}/{cid}")
	public String totalprice(Model model, @PathVariable(value = "id") int id, @PathVariable(value = "cid") int cid,
			@RequestParam("quatity") int quantity, RedirectAttributes redirectAttributes) {

		int h = 0;
		Customer customer = customerservice.getCustomerById(cid);

		if (quantity > 0) {
			CustomerCart cartId = customerCartService.getCartById(id);
			int q = quantity + cartId.getQuantity();
			List<PhysicalProducts> modelnumberlist = physicalProductRepository
					.getPhysicalProductsByModelNumber(cartId.getProductModelNumber());
			List<CustomerCart> customerCartlist = customerCartRepository.getAllcartListforproduct(cid,
					cartId.getProductModelNumber());
			ArrayList<Integer> cartIds = new ArrayList<>();
			for (CustomerCart c : customerCartlist) {
				cartIds.add(c.getProductId());
			}
			if (q < modelnumberlist.size()) {
				for (PhysicalProducts product : modelnumberlist) {
					for (int i = h; i < quantity; i++) {
						if (cartIds.contains(product.getProductId())) {
							continue;

						} else {
							CustomerCart cart = new CustomerCart();
							cart.setCartId((int) Math.random());
							cart.setImage(product.getProductImage());
							cart.setProductId(product.getProductId());
							cart.setProductName(product.getProductName());
							cart.setProductModelNumber(product.getProductModelNumber());
							cart.setProductPrice(product.getProductMRPPrice());
							cart.setProductCompany(product.getProductCompany());
							cart.setProductCode(product.getProductCode());
							cart.setIsActive('N');
							cart.setCustomerId(cid);
							customerCartService.addCart(cart);
							i = quantity + 1;
							h++;
						}
					}

				}

				model.addAttribute("msg", "only " + " " + modelnumberlist.size() + " are avaliable");

				Double total = cartId.getTotalprice() * q;
				cartId.setTotalprice(total);
				cartId.setQuantity(cartId.getQuantity() + quantity);
				customerCartService.addCart(cartId);
			}
			float totalcartprice = 0;
			List<CustomerCart> list = customerCartRepository.getCartActiveList(customer.getCustomerId());
			Customer data = customerservice.getCustomerById(cid);

			if (customerCartRepository.getcarttotal(cid) != null) {
				totalcartprice = Float.parseFloat(customerCartRepository.getcarttotal(cid));
			} else {
				totalcartprice = 0;
			}
			model.addAttribute("totalcartprice", totalcartprice);
			model.addAttribute("Cart", list);
			model.addAttribute("customer", data);
			model.addAttribute("msg1", "");

			return "cart";

		} else {
			redirectAttributes.addFlashAttribute("message", "Add valid quantity Number");

			return "redirect:/cart/cartList/" + customer.getCustomerId();

		}
	}

	@RequestMapping("/categoryviews/{cid}")
	public String PhysicalCategoryProducts(Model model, @RequestParam("categoryName") String categoryName,
			@PathVariable(value = "cid") int cid) {
		Customer customer = customerservice.getCustomerById(cid);
		model.addAttribute("customer", customer);
		List<PhysicalProducts> products = physicalProductRepository.findProductsBycategories(categoryName);
		model.addAttribute("products", products);
		List<PhysicalCategory> catagorylist = PhysicalCategoryService.getAllCategory();
		model.addAttribute("catagorylist", catagorylist);
		return "categoryviewphysicalproducts";

	}

	@RequestMapping("/addtocartthroughquantity/{pid}/{cid}")
	public String addtocartthroughquantity(@PathVariable("pid") int productid, @PathVariable("cid") int customerid,
			Model model, @RequestParam("quantity") int quantity,RedirectAttributes redirectAttributes) {
		PhysicalProducts physicalProducts = physicalProductRepository.getById(productid);
		Customer customer = customerservice.getCustomerById(customerid);
		List<CustomerCart> carts = customerCartRepository.getCartActiveList(customerid);
		List<PhysicalProducts> pList = physicalProductRepository
				.getPhysicalProductsByModelNumber(physicalProducts.getProductModelNumber());
		ArrayList<Integer> cartIds = new ArrayList<>();
		for (CustomerCart c : carts) {
			cartIds.add(c.getCartId());
		}
		int h = 0;
		int m = 0;
		if (quantity > 0&& physicalProducts.getQuantity()>=quantity) {
			
			if (cartIds.contains(physicalProducts.getProductId())) {
				CustomerCart cart = customerCartRepository.cartproductbyproductid(productid);
				m = cart.getQuantity() + quantity;
				if (physicalProducts.getQuantity() >= m) {
					cart.setQuantity(m);
					cart.setTotalprice(m * cart.getProductPrice());
					customerCartRepository.save(cart);
					for (PhysicalProducts product : pList) {
						if (cartIds.contains(product.getProductId())) {
							continue;
						} else {
							CustomerCart cart1 = new CustomerCart();
							cart1.setCartId((int) Math.random());
							cart1.setImage(product.getProductImage());
							cart1.setCustomerId(customerid);
							cart1.setProductId(product.getProductId());
							cart1.setStore(product.getStoreName());
							cart1.setProductName(product.getProductName());
							cart1.setProductModelNumber(product.getProductModelNumber());
							cart1.setProductPrice(product.getProductMRPPrice());
							cart1.setProductCompany(product.getProductCompany());
							cart1.setProductCode(product.getProductCode());
							cart1.setQuantity(1);
							cart1.setIsActive('N');
							customerCartService.addCart(cart1);

							List<CustomerCart> cList = customerCartRepository.getAllcartListforproduct(customerid,
									physicalProducts.getProductModelNumber());
							if (cList.size() == m) {
								break;
							}
						}
					}
				} else {
					return "redirect:/frontendproductdetails/" + productid + "/" + customerid;
				}
			} else {
				CustomerCart cart = new CustomerCart();
				cart.setCartId((int) Math.random());
				cart.setImage(physicalProducts.getProductImage());
				cart.setCustomerId(customerid);
				cart.setProductId(physicalProducts.getProductId());
				cart.setProductName(physicalProducts.getProductName());
				cart.setProductModelNumber(physicalProducts.getProductModelNumber());
				cart.setProductPrice(physicalProducts.getProductMRPPrice());
				cart.setProductCompany(physicalProducts.getProductCompany());
				cart.setProductCode(physicalProducts.getProductCode());
				cart.setStore(physicalProducts.getStoreName());
				cart.setIsActive('Y');
				cart.setTotalprice(physicalProducts.getProductMRPPrice());
				cart.setQuantity(quantity);
				customerCartService.addCart(cart);
				for (PhysicalProducts product : pList) {
					if (cartIds.contains(product.getProductId())) {
						continue;
					} else {
						CustomerCart cart1 = new CustomerCart();
						cart1.setCartId((int) Math.random());
						cart1.setImage(product.getProductImage());
						cart1.setCustomerId(customerid);
						cart1.setProductId(product.getProductId());
						cart1.setStore(product.getStoreName());
						cart1.setProductName(product.getProductName());
						cart1.setProductModelNumber(product.getProductModelNumber());
						cart1.setProductPrice(product.getProductMRPPrice());
						cart1.setProductCompany(product.getProductCompany());
						cart1.setProductCode(product.getProductCode());
						cart1.setQuantity(1);
						cart1.setIsActive('N');
						customerCartService.addCart(cart1);
						List<CustomerCart> cList = customerCartRepository.getAllcartListforproduct(customerid,
								physicalProducts.getProductModelNumber());
						if (cList.size() == quantity - 1) {
							break;
						}
					}
				}
				
			}
			return "redirect:/frontendproductdetails/" + productid + "/" + customerid;
		} else {
			if(quantity<=0) {
				redirectAttributes.addFlashAttribute("message", "Enter valid Quality");
			}else {
				redirectAttributes.addFlashAttribute("message", "It does not contain that much quantity "+quantity+ " it contain only "+physicalProducts.getQuantity());
			}
			return "redirect:/frontendproductdetails/" + productid + "/" + customerid;
		}
		
	}
	
}
