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

## What isnt working
- External storage and/or Share storage permissions
- Test set EXIF metadata and mediastore indexing

## Current Status
- Once I can either get external storage permissions working, or I can get the mediastore object to recognize upload images:
    - I will implement a bottom sheet overlay on the google maps screen which will show a list of images from users Pictures folder
    - Each image will have its GPS location marked on the google map with an annotation
    - There will be an option to save images to a S3 Bucket for cloud storage
     
