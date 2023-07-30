package me.djaydenr;


import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.model.checkout.SessionCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.QuoteCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import spark.Spark;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        Spark.port(4040);

        System.out.println("Webserver port is 4040");

        post("/payment", (request, response) -> {
            String invoicenumber = request.queryParams("invoice_number");
            String stripeapikey = request.queryParams("stripe_api_key");
            String amount = (request.queryParams("amount"));
            String currency = request.queryParams("currency");
            String customeremail = request.queryParams("email");
            String url = request.queryParams("url");
            String description = request.queryParams("description");
            String whmcsacceskey = request.queryParams("api_acces_key");
            String whmcsidentifier = request.queryParams("api_identifier_key");
            String whmcssecretkey = request.queryParams("api_secret_key");

            System.out.println(whmcsacceskey + " " + whmcsidentifier + " " + whmcssecretkey);

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(stripeapikey)
                    .build();


            long goodamount = (long) (Double.valueOf(amount) * 100L);

            System.out.println(goodamount);

            System.out.println(invoicenumber);

            Map<String, Object> paramspr = new HashMap<>();
            paramspr.put("name", "Invoice " + invoicenumber);

            Product product = Product.create(paramspr, requestOptions);


            Price price = Price.create(
                    PriceCreateParams.builder()
                            .setCurrency(currency)
                            .setUnitAmount(goodamount)
                            .setProduct(product.getId())
                            .build(), requestOptions
            );


            List<Object> lineItems = new ArrayList<>();
            Map<String, Object> lineItem1 = new HashMap<>();
            lineItem1.put("price", price.getId());
            lineItem1.put("quantity", 1);
            lineItems.add(lineItem1);
            Map<String, Object> params = new HashMap<>();
            params.put(
                    "success_url",
                    "https://payment.dr-it.dev/succes"
            );
            params.put(
                    "cancel_url",
                    url + "viewinvoice.php?id=" + invoicenumber + "&paymentfailed=true"
            );
            params.put("line_items", lineItems);
            params.put("mode", "payment");

            Session session = Session.create(params, requestOptions);

            response.redirect(session.getUrl());

            get("/succes", (request1, response1) -> {
                Map<String, Object> paramss = new HashMap<>();
                paramss.put("limit", 3);

                SessionCollection sessions = Session.list(paramss, requestOptions);
                if (sessions.getData().get(0).getPaymentStatus().equals("paid")){

                } else {
                    response1.redirect(url + "viewinvoice.php?id=" + invoicenumber + "&paymentfailed=true");
                }


                return "";
            });

            return "";
        });



    }
}
