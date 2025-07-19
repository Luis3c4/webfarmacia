package com.proyecto.farmacia.webfarmacia.paypal;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PaypalController {

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }


    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") String amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description

    ) {

        try {
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";

            Payment payment = paypalService.createPayment(
                    Double.valueOf(amount),
                    currency,
                    method,
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );

            for (Links link : payment.getLinks()) {
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    return new RedirectView(link.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            System.err.println("Error occurred::" + e.getMessage());
        }
        return new RedirectView("/payment/paypal/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerID
    ) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerID);
            if (payment.getState().equals("approved")) {
                System.out.println("Pago aprobado - Payment ID: " + paymentId + ", Payer ID: " + payerID);
                
                // El procesamiento de la venta se manejará en el frontend (paymentSuccess.html)
                // que llamará al endpoint /procesar-venta-paypal
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            System.err.println("Error occurred:: " + e.getMessage());
        }
        return "paymentError";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    @GetMapping("/payment/paypal/error")
    public String paymentError() {
        return "paymentError";
    }
}
