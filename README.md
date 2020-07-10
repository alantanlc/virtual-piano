# Virtual Piano

![screenshot](FYP16VirtualPiano_webimg.jpg)

# Description

This project develops an Android mobile application that uses real-time image processing techniques which allow users to play the piano on a piece of paper with piano keys drawn or printed on it.

The mobile device is placed at an angle for optimum view of the paper piano. When the application is started, the user switches on the piano and fingertip detection, fix the detected keys, and can then start playing on the paper piano.

The application tracks fingertips for finger presses on the prefixed piano keys and then plays the corresponding sounds. This application is developed using OpenCV, a library for real-time image processing with over 2500 optimized algorithms.

In piano detection, contours of white piano keys are found using border-following algorithm on binary thresholded images from captured frames in grayscale color space. Convex hull algorithm is then used to find the piano mask. Finally, the binary thresholded image is then inverted to find contours of black piano keys with the help of the piano mask.

In fingertip detection, skin-coloured contours are detected through Hue thresholding of captured frames in HSV colour space. The largest contour is assumed to be the hand. Douglas Peucker algorithm is used to reduce the number of contour points. Convex Hull algorithm is then applied to get convex hull of hand. Finally, the convex hull points are then used as the effective fingertip points.

A finger press occurs when the fingertip moves in a downward motion towards the paper piano. The downward motion is tracked by calculating the difference of the fingertip's y-coordinate points between two consecutive captured frames. If the difference is larger than a specific value, a finger press is deemed to have occurred. When a finger press is detected, the fingertip point in the second captured frame is used to check if the effective fingertip point falls within any white or black piano key contour region using point-in-contour test.

# Credits

To Mr. Ravi Suppiah for his guidance and the opportunity given to showcase my work during the NTU Open House 2016.
