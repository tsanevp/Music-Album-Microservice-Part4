package swagger

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
)

var serverError = "Internal Server Error"

type postResponse struct {
	ALBUMID   string `json:"albumID"`
	IMAGESIZE string `json:"imageSize"`
}

func GetAlbumByKey(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	urlString := r.URL.String()

	if urlString == "" {
		http.Error(w, "missing parameters", http.StatusNotFound)
		return
	}

	if !ValidUrl(urlString) {
		http.Error(w, "invalid request", http.StatusBadRequest)
		return
	}

	urlSplit := strings.Split(urlString, "/")
	albumId := urlSplit[3]

	if albumId != "" {
		responseJSON, jsonErr := json.Marshal(albums[0])

		if jsonErr != nil {
			http.Error(w, serverError, http.StatusInternalServerError)
			return
		}

		w.WriteHeader(http.StatusOK)
		_, writeErr := w.Write(responseJSON)

		if writeErr != nil {
			http.Error(w, serverError, http.StatusInternalServerError)
			return
		}
	} else {
		w.WriteHeader(http.StatusNotFound)
		_, keyErr := w.Write([]byte("Key not found"))

		if keyErr != nil {
			http.Error(w, serverError, http.StatusInternalServerError)
			return
		}
	}
}

func NewAlbum(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	err := r.ParseMultipartForm(10 << 20)

	if err != nil {
		http.Error(w, "Unable to parse form", http.StatusBadRequest)
		return
	}

	// artist := r.FormValue("artist")
	// title := r.FormValue("title")
	// year := r.FormValue("year")

	image, handler, err := r.FormFile("image")

	// if err != nil || artist == "" || title == "" || year == "" {
	if err != nil {
		http.Error(w, "Unable to get image or form fields", http.StatusBadRequest)
		return
	}
	defer image.Close()

	sizeAsString := strconv.FormatInt(handler.Size, 10)
	response := postResponse{ALBUMID: handler.Filename, IMAGESIZE: sizeAsString}

	responseJSON, jsonErr := json.Marshal(response)

	if jsonErr != nil {
		http.Error(w, serverError, http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
	_, writeErr := w.Write(responseJSON)

	if writeErr != nil {
		http.Error(w, serverError, http.StatusInternalServerError)
		return
	}
}

func ValidUrl(url string) bool {
	for _, endpoint := range endpoints {
		if endpoint.Pattern.MatchString(url) {
			return true
		}
	}

	return false
}
