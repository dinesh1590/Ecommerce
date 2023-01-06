package com.ecom.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.beans.BilingDetails;
import com.ecom.beans.Coupon;
import com.ecom.beans.Customer;
import com.ecom.beans.CustomerAddress;
import com.ecom.beans.CustomerCart;
import com.ecom.beans.CustomerOrder;
import com.ecom.beans.OrderByCustomer;
import com.ecom.beans.PhysicalProducts;
import com.ecom.beans.Store;
import com.ecom.repository.BillingRepository;
import com.ecom.repository.CouponRepository;
import com.ecom.repository.CustomerAddressRepository;
import com.ecom.repository.CustomerCartRepository;
import com.ecom.repository.CustomerOrderRepository;
import com.ecom.repository.CustomerRepository;
import com.ecom.repository.OrderByCustomerRepository;
import com.ecom.repository.StoreRepository;
import com.ecom.service.BillingService;
import com.ecom.service.CustomerCartService;
import com.ecom.service.CustomerService;
import com.ecom.service.EmailSenderService;
import com.ecom.service.PhysicalProductService;
import com.ecom.service.StoreService;

@Controller
@RequestMapping("/Bill")
public class BillingController {

	@Autowired
	BillingService billingService;
	@Autowired
	CustomerCartService customerCartService;
	@Autowired
	CouponRepository couponRepository;
	@Autowired
	BillingRepository billingRepository;
	@Autowired
	StoreService storeService;
	@Autowired
	StoreRepository storeRepository;
	@Autowired
	CustomerService customerService;
	@Autowired
	CustomerCartRepository customerCartRepository;
	@Autowired
	CustomerOrderRepository customerOrderRepository;
	@Autowired
	PhysicalProductService physicalProductService;
	@Autowired
	OrderByCustomerRepository orderByCustomerRepository;
	@Autowired
	EmailSenderService emailSenderService;
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	CustomerAddressRepository customerAddressRepository;

	@RequestMapping("/addressList/{cid}")
	public String addressingList(@PathVariable("cid") int customerId, Model model) {
		Customer customer = customerRepository.getById(customerId);
		List<CustomerAddress> customerAddress = customerAddressRepository.getCustomerAddress(customerId);
		model.addAttribute("address", customerAddress);
		model.addAttribute("customer", customer);
		return "Customer-Address-List";
	}

	@RequestMapping(value = "/indexBill/{cid}/{caid}")
	public String indexBill(Model model, @PathVariable(value = "cid") int customerId,
			@PathVariable("caid") int customerAddressId, RedirectAttributes redirectAttributes) {
		Customer customerobject = customerService.getCustomerById(customerId);
		List<CustomerCart> cartlist = customerCartRepository.getCartActiveList(customerId);
		if (cartlist.size() != 0) {
			CustomerAddress customerAddress = customerAddressRepository.getById(customerAddressId);
			BilingDetails bilingDetails = new BilingDetails();
			bilingDetails.setBillingId((int) Math.random());
			bilingDetails.setAddressId(customerAddress.getAddressId());
			bilingDetails.setCustomerId(customerId);
			bilingDetails.setAddressLine1(customerAddress.getAddressLine1());
			bilingDetails.setAddressLine2(customerAddress.getAddressLine2());
			bilingDetails.setCity(customerAddress.getCity());
			bilingDetails.setCountry(customerAddress.getCountry());
			bilingDetails.setFirstName(customerobject.getFirstName());
			bilingDetails.setLastName(customerobject.getLastName());
			bilingDetails.setPinCode(customerAddress.getPinCode());
			bilingDetails.setIsActive('Y');
			BilingDetails bill = billingRepository.save(bilingDetails);
			float total = Float.parseFloat(customerCartRepository.getcarttotal(customerId));

			return "redirect:/Bill/customerCheckout/" + customerId + "/" + bill.getBillingId() + "/" + total;

		} else {
			redirectAttributes.addFlashAttribute("message", "No Products in Cart");
			return "redirect:/cart/cartList/" + customerobject.getCustomerId();
		}
	}

	@RequestMapping("/customerCheckout/{cid}/{bid}/{total}")
	public String customerCheckout(Model model, @PathVariable("cid") int customerId, @PathVariable("bid") int billingId,
			@PathVariable("total") float total) {
		Customer customerobject = customerService.getCustomerById(customerId);
		List<CustomerCart> cartlist = customerCartRepository.getCartActiveList(customerId);
		BilingDetails bilingDetails = billingRepository.getById(billingId);
		CustomerAddress customerAddress = customerAddressRepository.getById(bilingDetails.getAddressId());
		List<Coupon> coupon = couponRepository.findAll();
		model.addAttribute("customer", customerobject);
		model.addAttribute("objBilling", bilingDetails);
		model.addAttribute("total", total);
		model.addAttribute("cartlist", cartlist);
		model.addAttribute("customerAddress", customerAddress);
		if (coupon.size() != 0) {
			model.addAttribute("coupon", coupon);
			return "checkout";
		} else {
			model.addAttribute("msg1", "No Coupon are Avaliable");
			return "checkout";
		}
	}

	@RequestMapping("/customerBilling/{total}/{payment}/{bid}")
	public String customerBilling(Model model, @PathVariable("bid") int bilingId, @PathVariable("total") float total,
			RedirectAttributes redirectAttributes, @PathVariable("payment") String paymentMethod) {
		int min1 = 1000000;
		int max2 = 9999999;
		int random_int1 = (int) Math.floor(Math.random() * (max2 - min1 + 1) + min1);
		int min = 10000;
		int max = 99999;
		int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
		BilingDetails bilingDetails=billingRepository.getById(bilingId);
		CustomerOrder order = new CustomerOrder();
		Customer customer = customerService.getCustomerById(bilingDetails.getCustomerId());

		order.setOrderId((int) Math.random());
		order.setOrderDate(LocalDate.now());
		order.setOrderTime(LocalTime.now());
		order.setIncrDate(LocalDate.now().plusDays(6));
		order.setCarrierId(random_int1);
		order.setCustomerId(bilingDetails.getCustomerId());
		order.setOrderNumber(random_int);
		order.setOrderDate(LocalDate.now());
		order.setOrderTime(LocalTime.now());
		order.setBillingId(bilingId);
		order.setPaymentMethod(paymentMethod);
		order.setStatus("Delivared");
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
		StringBuilder sb = new StringBuilder(17);

		for (int i = 0; i < 16; i++) {
			int index = (int) (AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}
		String str = sb.toString();
		order.setTranscationId(str);
		order.setTotalPrice(total);
		order.setCustomerId(bilingDetails.getCustomerId());

		CustomerOrder orderobject = customerOrderRepository.save(order);

		List<CustomerCart> cartlist1 = customerCartRepository.getAllCartList(bilingDetails.getCustomerId());
		for (CustomerCart cart : cartlist1) {

			cart.setIsActive('B');
			customerCartService.addCart(cart);
		}
		model.addAttribute("totalcartprice", customerCartRepository.getBilltotal(bilingDetails.getCustomerId()));

		List<CustomerCart> quantity = customerCartRepository.getBillOrderList(bilingDetails.getCustomerId());
		for (CustomerCart products : quantity) {
			List<OrderByCustomer> object1 = orderByCustomerRepository.ordered(bilingDetails.getCustomerId(),
					products.getProductModelNumber(), orderobject.getOrderId());
			if (object1.size() == 0) {
				OrderByCustomer orderByCustomer = new OrderByCustomer();
				orderByCustomer.setOBCId((int) Math.random());
				orderByCustomer.setOrderId(orderobject.getOrderId());
				orderByCustomer.setCustomerId(bilingDetails.getCustomerId());
				orderByCustomer.setImage(products.getImage());
				orderByCustomer.setCustomerId(bilingDetails.getCustomerId());
				orderByCustomer.setProductId(products.getProductId());
				orderByCustomer.setProductName(products.getProductName());
				orderByCustomer.setProductModelNumber(products.getProductModelNumber());
				orderByCustomer.setProductPrice(products.getProductPrice());
				orderByCustomer.setProductCompany(products.getProductCompany());
				orderByCustomer.setProductCode(products.getProductCode());
				orderByCustomer.setIsActive('Y');
				orderByCustomer.setTotalprice(products.getProductPrice());
				orderByCustomer.setStore(products.getStore());
				orderByCustomer.setQuantity(1);
				orderByCustomerRepository.save(orderByCustomer);

			} else {

				for (OrderByCustomer orderByCustomer1 : object1) {
					OrderByCustomer obj = orderByCustomerRepository.getById(orderByCustomer1.getOBCId());
					obj.setQuantity(obj.getQuantity() + 1);
					obj.setTotalprice(obj.getProductPrice() + obj.getProductPrice());

					orderByCustomerRepository.save(obj);

				}

			}
		}
		return "redirect:/Bill/orderSuccess/" + orderobject.getOrderId() + "/" + bilingId;
	}

	@RequestMapping(value = "/AddBill/{cid}/{total}/{caid}")
	public String addBill(Model model, BilingDetails bilingDetails,
			@PathVariable(value = "cid") int customerId, @PathVariable("total") float total,
			@RequestParam("payment-group") String paymentMethod, @PathVariable("caid") int customerAddressId,
			RedirectAttributes redirectAttributes) {
		bilingDetails.setCustomerId(customerId);
		bilingDetails.setIsActive('Y');
		bilingDetails.setAddressId(customerAddressId);
		BilingDetails billingobject = billingService.addBilingName(bilingDetails);
		if (paymentMethod.equals("Cash On Delivery") && total > 20000) {

			return "redirect:/Bill/customerCheckout/" + customerId + "/" + billingobject.getBillingId() + "/" + total;
		} else if (paymentMethod.equals("PayPal")) {

			return "redirect:/Bill/customerPaymentMethod/" + billingobject.getBillingId() + "/" + total;
		} else {

			return "redirect:/Bill/customerBilling/" + total + "/" + paymentMethod + "/" + billingobject.getBillingId();
		}
	}

	@RequestMapping("/customerPaymentMethod/{bid}/{total}")
	public String customerPaymentMethod(Model model, @PathVariable("bid") int bilingId,
			@PathVariable("total") float total) {
		BilingDetails bilingDetails = billingRepository.getById(bilingId);
		Customer customer = customerRepository.getById(bilingDetails.getCustomerId());
		CustomerAddress customerAddress = customerAddressRepository.getById(bilingDetails.getAddressId());
		model.addAttribute("customerAddress", customerAddress);
		model.addAttribute("customer", customer);
		model.addAttribute("bilingDetails", bilingDetails);
		model.addAttribute("total", total);
		return "card-payment";

	}

	@RequestMapping("/tracking/{cid}/{obcid}/{caid}/{bid}/{oid}")
	public String tracking(Model model, @PathVariable("cid") int customerId,
			@PathVariable("obcid") int orderByCustomerId, @PathVariable("caid") int customerAddressId,
			@PathVariable("bid") int billingId, @PathVariable("oid") int orderId) {
		Customer customer = customerService.getCustomerById(customerId);
		OrderByCustomer orderByCustomer = orderByCustomerRepository.getById(orderByCustomerId);
		CustomerAddress customerAddress = customerAddressRepository.getById(customerAddressId);
		BilingDetails bilingDetails = billingRepository.getById(billingId);
		CustomerOrder customerOrder = customerOrderRepository.getById(orderId);
		Store store = storeRepository.findStoreName(orderByCustomer.getStore());
		StringBuffer snlalo = new StringBuffer();
		snlalo.append("[\"" + store.getStoreName() + "\"," + store.getLatitude() + "," + store.getLongitude() + "," + 1
				+ "],");
		model.addAttribute("customer", customer);
		model.addAttribute("orderByCustomer", orderByCustomer);
		model.addAttribute("customerAddress", customerAddress);
		model.addAttribute("bilingDetails", bilingDetails);
		model.addAttribute("customerOrder", customerOrder);
		model.addAttribute("store", store);
		model.addAttribute("snlalo", snlalo.toString());
		return "order-tracking";
	}

	@RequestMapping("/applycoupon/{cpnId}/{cid}/{bid}/{caid}")
	public String applyCoupon(Model model, @PathVariable("cid") int customerId, @PathVariable("cpnId") int couponId,
			@PathVariable("bid") int billingId, @PathVariable("caid") int customerAddressId) {
		Customer customerobject = customerService.getCustomerById(customerId);
		CustomerAddress customerAddress = customerAddressRepository.getById(customerAddressId);
		List<CustomerCart> cartlist = customerCartRepository.getCartActiveList(customerId);
		BilingDetails billingDetails = billingRepository.getById(billingId);
		float total = Float.parseFloat(customerCartRepository.getcarttotal(customerId));
		Coupon coupon = couponRepository.getById(couponId);
		float cpnprice = (float) (coupon.getDiscount() / 100);
		total = total - cpnprice * total;
		model.addAttribute("customer", customerobject);
		model.addAttribute("objBilling", billingDetails);
		model.addAttribute("total", total);
		model.addAttribute("cartlist", cartlist);
		model.addAttribute("coupon", coupon);
		model.addAttribute("customerAddress", customerAddress);
		return "coupon-checkout";
	}

	@RequestMapping("/addAddress/{cid}")
	public String addAddress(@PathVariable("cid") int customerId, Model model) {
		Customer customer = customerRepository.getById(customerId);
		CustomerAddress address = new CustomerAddress();
		model.addAttribute("address", address);
		model.addAttribute("customer", customer);
		return "customer-address";
	}

	@RequestMapping("/saveAddress/{cid}")
	public String saveAddress(@PathVariable("cid") int customerId, Model model, CustomerAddress address) {
		address.setIsActive('Y');
		address.setCustomerId(customerId);
		customerAddressRepository.save(address);
		Customer customer = customerRepository.getById(customerId);
		List<CustomerAddress> customerAddress = customerAddressRepository.getCustomerAddress(customerId);
		model.addAttribute("address", customerAddress);
		model.addAttribute("customer", customer);
		return "Customer-Address-List";
	}

	@RequestMapping("/deleteAddress/{cid}/{caid}")
	public String deleteAddress(@PathVariable("cid") int customerId, @PathVariable("caid") int customerAddressId,
			Model model) {
		customerAddressRepository.deleteById(customerAddressId);
		Customer customer = customerRepository.getById(customerId);
		List<CustomerAddress> customerAddress = customerAddressRepository.getCustomerAddress(customerId);
		model.addAttribute("address", customerAddress);
		model.addAttribute("customer", customer);
		model.addAttribute("msg1", "Delete address Successfully");
		return "Customer-Address-List";
	}

	@RequestMapping("/billingAddress/{cid}")
	public String billingAddress(@PathVariable("cid") int customerId, Model model) {
		Customer customer = customerRepository.getById(customerId);
		List<BilingDetails> bilingDetails = billingRepository.getData(customerId);
		model.addAttribute("address", bilingDetails);
		model.addAttribute("customer", customer);
		return "Customer-Address-List";
	}

	@RequestMapping("/orderSuccess/{oid}/{bid}")
	public String orderSuccess( @PathVariable("oid") int orderId,
			@PathVariable("bid") int bid, Model model) {
		BilingDetails bilingDetails = billingRepository.getById(bid);
		CustomerAddress customerAddress = customerAddressRepository.getById(bilingDetails.getAddressId());
		CustomerOrder customerOrder = customerOrderRepository.getById(orderId);
		Customer customer = customerRepository.getById(bilingDetails.getCustomerId());
		List<OrderByCustomer> orderDownload = orderByCustomerRepository.orderedProduct(customerOrder.getOrderId(),
				customer.getCustomerId());
		model.addAttribute("totalcartprice", customerCartRepository.getBilltotal(customer.getCustomerId()));
		model.addAttribute("customerAddress", customerAddress);
		model.addAttribute("Cart", orderDownload);
		model.addAttribute("orderobject", customerOrder);
		model.addAttribute("billingobject", bilingDetails);
		model.addAttribute("customer", customer);
		return "order-success";
	}
}
