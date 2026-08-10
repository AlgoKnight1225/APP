package com.canteen.foodordering.models;

public class PromoBanner {
    private String title;
    private String subtitle;
    private String promoCode;
    private String imageUrl;

    public PromoBanner() {
    }

    public PromoBanner(String title, String subtitle, String promoCode, String imageUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.promoCode = promoCode;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
