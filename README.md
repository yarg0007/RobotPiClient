# RobotPiClient
===============
Client control application that sends commands to RobotPiServer instance.

##Video Streaming
---
Test streaming video from raspberry pi over network.

1. On your Mac:  
Install XQuartz: https://xquartz.macosforge.org  
install homebrew: http://brew.sh/
Install gstreamer 
```
brew install gstreamer gst-libav gst-plugins-ugly gst-plugins-base gst-plugins-bad gst-plugins-good
```

2. On raspberry pi (NOTE THAT EDITING sources.list IS NO LONGER REQUIRED. GSTREAMER IS PART OF RASPBIAN):  
```
sudo vi /etc/apt/sources.list  
# and add to the end: deb http://vontaene.de/raspbian-updates/ . main
sudo apt-get update
sudo apt-get install gstreamer1.0
```

3. Start streaming from raspberry pi:  
```
raspivid -t 999999 -h 720 -w 1080 -fps 25 -hf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse !  rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=YOUR-PI-IP-ADDRESS port=5000
```

4. Receive stream on Mac:  
```
gst-launch-1.0 -v tcpclientsrc host=YOUR-PI-IP-ADDRESS port=5000  ! gdpdepay !  rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false
```

Source: http://blog.tkjelectronics.dk/2013/06/how-to-stream-video-and-audio-from-a-raspberry-pi-with-no-latency/  
Source: http://pi.gbaman.info/?p=150

