package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import entities.Reservation;
import entities.local;

import java.util.ArrayList;
import java.util.List;

public class StripePaymentService {

    private static final String API_KEY = "sk_test_51QwT1qGYMYTWr0mcaKRIM2AmiC0NVn2Uip1ANuBCnOWLnJSCicVlIwLMnpt9wgCxO1sfO1qCu2IFHe3TlU7FITM500W7gdd8YD";
    private static final String PUBLIC_KEY = "pk_test_51QwT1qGYMYTWr0mcazJPIc0PXtljbf8hYONclLnz42IKjLqZJk9BiWYrT74FAsQhJrwklPxn69ZEtkozDk8t4Og800rMVaZI0y";

    private final ServiceLocal serviceLocal = new ServiceLocal();

    public StripePaymentService() {
        // Initialize Stripe with the API key
        Stripe.apiKey = API_KEY;
    }

    /**
     * Create a Stripe Checkout Session for the given reservations
     * @param reservations List of reservations to be paid
     * @param successUrl URL to redirect to after successful payment
     * @param cancelUrl URL to redirect to after cancelled payment
     * @return Checkout session URL
     * @throws StripeException if there's an error creating the session
     */
    public String createCheckoutSession(List<Reservation> reservations, String successUrl, String cancelUrl) throws StripeException {
        if (reservations == null || reservations.isEmpty()) {
            throw new IllegalArgumentException("Reservations list cannot be empty");
        }

        // Build line items from reservations
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (Reservation reservation : reservations) {
            local venue = serviceLocal.rechercherLocalParId(reservation.getLocalId());
            String venueName = venue != null ? venue.getNom() : "Venue " + reservation.getLocalId();
            String description = venue != null && venue.getDescription() != null
                    ? venue.getDescription()
                    : "Reservation for " + venueName + " on " + reservation.getDate();

            // Create a line item for this reservation
            SessionCreateParams.LineItem item = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (reservation.getPrix() * 100)) // Stripe uses cents
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(venueName)
                                                    .setDescription(description)
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(1L)
                    .build();

            lineItems.add(item);
        }

        // Create the checkout session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Redirects to the payment page for a list of reservations
     * @param reservations List of reservations to pay for
     * @return URL to redirect to for payment
     * @throws StripeException if there's an error creating the payment session
     */
    public String checkout(List<Reservation> reservations) throws StripeException {
        // Use custom URL schemes that can be detected by the WebView
        String successUrl = "https://eventaplan.com/payment/success";
        String cancelUrl = "https://eventaplan.com/payment/cancel";

        return createCheckoutSession(reservations, successUrl, cancelUrl);
    }

    /**
     * Get the public key for Stripe integration on the client side
     * @return Public key
     */
    public static String getPublicKey() {
        return PUBLIC_KEY;
    }
}