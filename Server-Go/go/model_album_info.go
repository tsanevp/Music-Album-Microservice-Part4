package swagger

type AlbumInfo struct {
	// album performers
	Artist string `json:"artist,omitempty"`
	// album title
	Title string `json:"title,omitempty"`
	// year released
	Year string `json:"year,omitempty"`
}
