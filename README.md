# GeoSnap

GeoSnap is an Android application that displays user images on a Google Map with location markers based on GPS metadata extracted from the images' EXIF data.

## Features

- **Google Maps Integration**: View a map with markers indicating locations where photos were taken.
- **EXIF Metadata Extraction**: Automatically extracts GPS coordinates from the EXIF metadata of images stored on the device.
- **Photo Library Access**: Reads and processes images from the device's external storage to map their locations.
- **Dynamic Permissions**: Requests necessary storage permissions from the user.


## What is working
- Google maps 
- User location permissions
- Http connection to Ruby Api
- External storage permissions

## What is broken
- everything related to handling exif extraction/storage except MetadataExample.java test

## Current Status
-External storage permissions are now granted
    -since app uses external storage permissions from before API 30+, it cannot be used on google play store
        -in the future can get around this issue through this route: "select file from storage and copy that file into your app package chache com.android.myapp"
            found in this thread: https://stackoverflow.com/questions/62782648/android-11-scoped-storage-permissions

- Next, creating bottom sheet to view/sort images onto google map
    - adding algorithms for image selection by album/day/week/moth/year
- Adding annotations to map at image gps location
    - hover over annotation to see image
     
### Login Screen
![Login Screen](https://github.com/peterbusche/GeoSnap/blob/main/Screenshot%202025-01-06%20103733.png?raw=true)

### Google Maps API
![Google Maps](https://github.com/peterbusche/GeoSnap/blob/main/Screenshot%202025-01-06%20103832.png?raw=true)

