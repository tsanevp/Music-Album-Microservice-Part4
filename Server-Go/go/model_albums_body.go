package swagger

import (
	"os"
)

type AlbumsBody struct {
	Image **os.File `json:"image,omitempty"`

	Profile *AlbumsProfile `json:"profile,omitempty"`
}
