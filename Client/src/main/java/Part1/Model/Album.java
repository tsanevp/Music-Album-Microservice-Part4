package Part1.Model;

import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.ImageMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Album {

    @NotNull
    private String requestType;

    @Nullable
    private AlbumInfo albumInfo;

    @Nullable
    private ImageMetaData imageMetaData;

    public Album(@NotNull String requestType, @Nullable AlbumInfo albumInfo, @Nullable ImageMetaData imageMetaData) {
        this.requestType = requestType;
        this.albumInfo = albumInfo;
        this.imageMetaData = imageMetaData;
    }


    public @NotNull String getRequestType() {
        return this.requestType;
    }

    public void setRequestType(@NotNull String requestType) {
        this.requestType = requestType;
    }


    public @Nullable AlbumInfo getAlbumInfo() {
        return this.albumInfo;
    }

    public void setAlbumInfo(@Nullable AlbumInfo albumInfo) {
        this.albumInfo = albumInfo;
    }

    public @Nullable ImageMetaData getImageMetaData() {
        return this.imageMetaData;
    }

    public void setImageMetaData(@Nullable ImageMetaData imageMetaData) {
        this.imageMetaData = imageMetaData;
    }
}