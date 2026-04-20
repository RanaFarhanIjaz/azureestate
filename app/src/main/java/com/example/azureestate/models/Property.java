package com.example.azureestate.models;

import java.util.List;

public class Property {
    private int id;
    private String title;
    private String address;
    private String price;
    private double rating;
    private int beds;
    private int baths;
    private String sqft;
    private int garages;
    private String badge;
    private String category;
    private int imageResId;
    private boolean isFavorited;
    private String description;
    private String agentName;
    private String agentTitle;
    private String agentQuote;
    private List<String> amenities;
    private String exclusiveTag;
    
    // NEW FIELDS for chat and ownership
    private String ownerId;
    private String ownerPhone;
    private List<String> photos;
    private String status; // "active", "sold", "pending"

    // Existing constructor
    public Property(int id, String title, String address, String price,
                    double rating, int beds, int baths, String sqft,
                    int garages, String badge, String category,
                    int imageResId, String description,
                    String agentName, String agentTitle, String agentQuote,
                    List<String> amenities, String exclusiveTag) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.rating = rating;
        this.beds = beds;
        this.baths = baths;
        this.sqft = sqft;
        this.garages = garages;
        this.badge = badge;
        this.category = category;
        this.imageResId = imageResId;
        this.description = description;
        this.agentName = agentName;
        this.agentTitle = agentTitle;
        this.agentQuote = agentQuote;
        this.amenities = amenities;
        this.exclusiveTag = exclusiveTag;
        this.isFavorited = false;
        this.status = "active";
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAddress() { return address; }
    public String getPrice() { return price; }
    public double getRating() { return rating; }
    public int getBeds() { return beds; }
    public int getBaths() { return baths; }
    public String getSqft() { return sqft; }
    public int getGarages() { return garages; }
    public String getBadge() { return badge; }
    public String getCategory() { return category; }
    public int getImageResId() { return imageResId; }
    public boolean isFavorited() { return isFavorited; }
    public String getDescription() { return description; }
    public String getAgentName() { return agentName; }
    public String getAgentTitle() { return agentTitle; }
    public String getAgentQuote() { return agentQuote; }
    public List<String> getAmenities() { return amenities; }
    public String getExclusiveTag() { return exclusiveTag; }
    
    // NEW Getters
    public String getOwnerId() { return ownerId; }
    public String getOwnerPhone() { return ownerPhone; }
    public List<String> getPhotos() { return photos; }
    public String getStatus() { return status; }

    // Setters
    public void setFavorited(boolean favorited) { isFavorited = favorited; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public void setStatus(String status) { this.status = status; }
}