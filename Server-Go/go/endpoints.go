package swagger

import (
	"regexp"
)

const (
	PostNewAlbumPattern  = `^/go/albums$`
	GetAlbumByKeyPattern = `^/go/albums/\d+$`
)

type Endpoint struct {
	Name    string
	Pattern *regexp.Regexp
}

var endpoints = []Endpoint{
	{Name: "POST_NEW_ALBUM", Pattern: regexp.MustCompile(PostNewAlbumPattern)},
	{Name: "GET_ALBUM_BY_ID", Pattern: regexp.MustCompile(GetAlbumByKeyPattern)},
}
