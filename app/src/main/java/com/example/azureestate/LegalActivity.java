package com.example.azureestate;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays legal / informational content: Privacy Policy, Terms of Service,
 * Help Center, or About.  Pass an EXTRA_TYPE string to select which page.
 */
public class LegalActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "legal_type";
    public static final String TYPE_PRIVACY  = "privacy";
    public static final String TYPE_TERMS    = "terms";
    public static final String TYPE_HELP     = "help";
    public static final String TYPE_ABOUT    = "about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        ImageView btnBack     = findViewById(R.id.btnBack);
        TextView  tvTitle     = findViewById(R.id.tvTitle);
        TextView  tvUpdated   = findViewById(R.id.tvLastUpdated);
        TextView  tvContent   = findViewById(R.id.tvContent);

        btnBack.setOnClickListener(v -> finish());

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = TYPE_PRIVACY;

        switch (type) {
            case TYPE_PRIVACY:
                tvTitle.setText("Privacy Policy");
                tvUpdated.setText("Last updated: January 2025");
                tvContent.setText(Html.fromHtml(getPrivacyPolicy(), Html.FROM_HTML_MODE_COMPACT));
                break;
            case TYPE_TERMS:
                tvTitle.setText("Terms of Service");
                tvUpdated.setText("Last updated: January 2025");
                tvContent.setText(Html.fromHtml(getTermsOfService(), Html.FROM_HTML_MODE_COMPACT));
                break;
            case TYPE_HELP:
                tvTitle.setText("Help Center");
                tvUpdated.setText("Need assistance? We're here to help.");
                tvContent.setText(Html.fromHtml(getHelpCenter(), Html.FROM_HTML_MODE_COMPACT));
                break;
            case TYPE_ABOUT:
                tvTitle.setText("About Azure Estate");
                tvUpdated.setText("Version 1.0.0");
                tvContent.setText(Html.fromHtml(getAbout(), Html.FROM_HTML_MODE_COMPACT));
                break;
        }
    }

    // ───────────────────────────────────────────────────────────────
    //  PRIVACY POLICY
    // ───────────────────────────────────────────────────────────────
    private String getPrivacyPolicy() {
        return "<b>1. Information We Collect</b><br/><br/>" +
                "When you use Azure Estate, we may collect the following information:<br/><br/>" +
                "• <b>Account Information:</b> Your name, email address, and profile picture when you sign in with Google.<br/>" +
                "• <b>Property Listings:</b> Photos, descriptions, pricing, and location data you provide when listing a property.<br/>" +
                "• <b>Usage Data:</b> How you interact with the app, including searches, favorites, and pages visited.<br/>" +
                "• <b>Device Information:</b> Device type, operating system version, and unique device identifiers.<br/><br/>" +

                "<b>2. How We Use Your Information</b><br/><br/>" +
                "We use the information we collect to:<br/><br/>" +
                "• Provide, maintain, and improve our services.<br/>" +
                "• Personalize your experience and show relevant property listings.<br/>" +
                "• Facilitate communication between buyers and sellers.<br/>" +
                "• Send important notifications about your account or listings.<br/>" +
                "• Ensure the safety and security of our platform.<br/><br/>" +

                "<b>3. Data Sharing</b><br/><br/>" +
                "We do not sell your personal information. We may share your information with:<br/><br/>" +
                "• <b>Other Users:</b> Your public profile and listing information is visible to other users.<br/>" +
                "• <b>Service Providers:</b> Third-party services that help us operate the app (e.g., Firebase, Supabase).<br/>" +
                "• <b>Legal Requirements:</b> When required by law or to protect our rights.<br/><br/>" +

                "<b>4. Data Storage & Security</b><br/><br/>" +
                "Your data is stored securely using industry-standard encryption. We use Firebase Authentication for secure sign-in and Supabase for database operations. We implement appropriate security measures to protect against unauthorized access, alteration, or destruction of your data.<br/><br/>" +

                "<b>5. Your Rights</b><br/><br/>" +
                "You have the right to:<br/><br/>" +
                "• Access and download your personal data.<br/>" +
                "• Correct inaccurate information.<br/>" +
                "• Delete your account and associated data.<br/>" +
                "• Opt out of non-essential communications.<br/><br/>" +

                "<b>6. Children's Privacy</b><br/><br/>" +
                "Azure Estate is not intended for users under the age of 18. We do not knowingly collect information from children.<br/><br/>" +

                "<b>7. Changes to This Policy</b><br/><br/>" +
                "We may update this Privacy Policy from time to time. We will notify you of any significant changes through the app or via email.<br/><br/>" +

                "<b>8. Contact Us</b><br/><br/>" +
                "If you have questions about this Privacy Policy, please contact us at:<br/>" +
                "📧 support@azureestate.com";
    }

    // ───────────────────────────────────────────────────────────────
    //  TERMS OF SERVICE
    // ───────────────────────────────────────────────────────────────
    private String getTermsOfService() {
        return "<b>1. Acceptance of Terms</b><br/><br/>" +
                "By accessing or using Azure Estate, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use the app.<br/><br/>" +

                "<b>2. Description of Service</b><br/><br/>" +
                "Azure Estate is a real estate platform that allows users to browse, search, and list properties for sale or rent. The app provides tools for property discovery, communication between parties, and AI-powered assistance.<br/><br/>" +

                "<b>3. User Accounts</b><br/><br/>" +
                "• You must provide accurate and complete information when creating an account.<br/>" +
                "• You are responsible for maintaining the security of your account credentials.<br/>" +
                "• You must not share your account or let others access your account.<br/>" +
                "• We reserve the right to suspend or terminate accounts that violate these terms.<br/><br/>" +

                "<b>4. Property Listings</b><br/><br/>" +
                "• You are solely responsible for the accuracy of your property listings.<br/>" +
                "• Listings must not contain false, misleading, or fraudulent information.<br/>" +
                "• You must have the legal right to list any property you post.<br/>" +
                "• We reserve the right to remove listings that violate our guidelines.<br/><br/>" +

                "<b>5. Prohibited Conduct</b><br/><br/>" +
                "You agree not to:<br/><br/>" +
                "• Post false or misleading property information.<br/>" +
                "• Harass, threaten, or abuse other users.<br/>" +
                "• Use the app for any unlawful purpose.<br/>" +
                "• Attempt to circumvent security measures.<br/>" +
                "• Scrape, copy, or redistribute content without permission.<br/>" +
                "• Impersonate another person or entity.<br/><br/>" +

                "<b>6. Intellectual Property</b><br/><br/>" +
                "All content, features, and functionality of Azure Estate — including design, text, graphics, and logos — are owned by Azure Estate and protected by intellectual property laws.<br/><br/>" +

                "<b>7. Disclaimer of Warranties</b><br/><br/>" +
                "Azure Estate is provided \"as is\" and \"as available\" without warranties of any kind. We do not guarantee the accuracy of property listings or the reliability of other users.<br/><br/>" +

                "<b>8. Limitation of Liability</b><br/><br/>" +
                "Azure Estate shall not be liable for any indirect, incidental, special, or consequential damages arising from your use of the app. Our total liability shall not exceed the amount you paid (if any) for using our services.<br/><br/>" +

                "<b>9. Changes to Terms</b><br/><br/>" +
                "We may modify these Terms of Service at any time. Continued use of the app after changes constitutes acceptance of the new terms.<br/><br/>" +

                "<b>10. Contact Us</b><br/><br/>" +
                "For questions about these Terms of Service, contact us at:<br/>" +
                "📧 support@azureestate.com";
    }

    // ───────────────────────────────────────────────────────────────
    //  HELP CENTER
    // ───────────────────────────────────────────────────────────────
    private String getHelpCenter() {
        return "<b>🏠 Getting Started</b><br/><br/>" +
                "<b>How do I create an account?</b><br/>" +
                "You can sign up using your email address or sign in with your Google account. Tap the \"Sign Up\" button on the login screen to get started.<br/><br/>" +

                "<b>How do I search for properties?</b><br/>" +
                "Use the Search tab to browse properties. You can filter by type, price range, number of bedrooms, bathrooms, and area size using the filter button.<br/><br/>" +

                "<b>How do I save a property to my favorites?</b><br/>" +
                "Tap the heart icon on any property card to add it to your favorites. You can view all your saved properties in the Favorites tab.<br/><br/>" +

                "<b>─────────────────────</b><br/><br/>" +

                "<b>📋 Listing Properties</b><br/><br/>" +
                "<b>How do I list a property?</b><br/>" +
                "Go to the Upload tab and fill in your property details including photos, description, price, location, and amenities. Follow the step-by-step process to publish your listing.<br/><br/>" +

                "<b>Can I edit my listing after publishing?</b><br/>" +
                "Currently, you can manage your listings through the \"My Properties\" section in your profile.<br/><br/>" +

                "<b>─────────────────────</b><br/><br/>" +

                "<b>💬 Messaging</b><br/><br/>" +
                "<b>How do I contact a property owner?</b><br/>" +
                "Open a property listing and tap the \"Contact Agent\" button or use the messaging feature to start a conversation.<br/><br/>" +

                "<b>Where are my messages?</b><br/>" +
                "You can find all your conversations in the Messages tab at the bottom of the screen or through your Profile → Messages.<br/><br/>" +

                "<b>─────────────────────</b><br/><br/>" +

                "<b>🤖 AI Assistant</b><br/><br/>" +
                "<b>What can the AI assistant do?</b><br/>" +
                "Our AI-powered chat assistant can help you find properties, answer questions about listings, and provide real estate guidance. Access it from the AI Chat tab.<br/><br/>" +

                "<b>─────────────────────</b><br/><br/>" +

                "<b>📧 Still Need Help?</b><br/><br/>" +
                "If you can't find the answer you're looking for, contact our support team:<br/><br/>" +
                "Email: support@azureestate.com<br/>" +
                "We typically respond within 24 hours.";
    }

    // ───────────────────────────────────────────────────────────────
    //  ABOUT
    // ───────────────────────────────────────────────────────────────
    private String getAbout() {
        return "<b>Azure Estate</b><br/><br/>" +
                "Azure Estate is a modern real estate platform designed to make property discovery seamless and enjoyable. Whether you're looking to buy, rent, or sell — we provide the tools to make your real estate journey effortless.<br/><br/>" +

                "<b>✦ Key Features</b><br/><br/>" +
                "• Browse thousands of property listings<br/>" +
                "• Advanced search with powerful filters<br/>" +
                "• AI-powered property assistant<br/>" +
                "• Real-time messaging with agents and owners<br/>" +
                "• Save and track your favorite properties<br/>" +
                "• Easy property listing with photo uploads<br/><br/>" +

                "<b>✦ Our Mission</b><br/><br/>" +
                "We believe finding your dream property should be simple, transparent, and accessible to everyone. Azure Estate combines cutting-edge technology with elegant design to deliver a premium real estate experience.<br/><br/>" +

                "<b>✦ Technology</b><br/><br/>" +
                "Built with care using modern technologies including Firebase, Supabase, and AI-powered assistance to deliver a fast, secure, and reliable experience.<br/><br/>" +

                "<b>Version:</b> 1.0.0<br/>" +
                "<b>Developer:</b> Azure Estate Team<br/>" +
                "© 2025 Azure Estate. All rights reserved.";
    }
}
