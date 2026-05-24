package com.traveling.travel_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.traveling.travel_backend.model.PlaceImage;

public class PlaceImageResponseDTO {

    private long id;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("public_id")
    private String publicId;

    @JsonProperty("alt_text")
    private String altText;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_main")
    private Boolean isMain;

    public PlaceImageResponseDTO() {}

    public PlaceImageResponseDTO(
            long id,
            String imageUrl,
            String publicId,
            String altText,
            Integer displayOrder,
            Boolean isMain
    ) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.isMain = isMain;
    }

    public static PlaceImageResponseDTO fromEntity(PlaceImage image) {
        return new PlaceImageResponseDTO(
                image.getId(),
                image.getImageUrl(),
                image.getPublicId(),
                image.getAltText(),
                image.getDisplayOrder(),
                image.getIsMain()
        );
    }

    public long getId() { return id; }

    public String getImageUrl() { return imageUrl; }

    public String getPublicId() { return publicId; }

    public String getAltText() { return altText; }

    public Integer getDisplayOrder() { return displayOrder; }

    public Boolean getIsMain() { return isMain; }
}