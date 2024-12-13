# Binny, the Smart Bin

Binny is an intelligent and interactive trash bin that automatically sorts waste while providing an engaging and educational experience for children. The project aims to combine technological innovation with eco-friendly practices, making recycling fun and efficient.

## Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Hardware Requirements](#hardware-requirements)
- [Setup and Installation](#setup-and-installation)
- [Possible Improvements](#possible-improvements)
- [Contributors](#contributors)
- [License](#license)

## Overview

Developed as part of the courses **INFOM450**, **INFOM451**, and **INFOM453** at Université de Namur, Binny addresses the needs of eco-conscious individuals with limited time, particularly parents wanting to teach their children about recycling. The bin uses AI to sort waste and interacts with children by sharing jokes and educational tips on recycling.

## Key Features

- **Automatic Waste Sorting**: Utilizes a camera and AI model to identify and sort waste in real-time.
- **Educational Interactions**: Engages children with interactive conversations and jokes about recycling.
- **Reliable Connectivity**: Supports both local and server-based operation with a robust REST-mesh architecture.

## Technologies Used

### Software
- **Scala** with Akka framework for sensor management and real-time data handling:
  - Event-driven actor system for seamless communication between components.
  - Animation features using ScalaFX for a child-friendly interface.
  - WebSocket for interaction between Python AI and Scala components.
- **Python** for AI and voice interaction:
  - Image recognition using [**YOLOv11**](https://docs.ultralytics.com/models/yolo11/).
  - Natural Language Processing with [**Llama 3.2**](https://www.llama.com/).
  - Voice synthesis using GLaDOS-inspired text-to-speech models.

### Hardware
- **Raspberry Pi 4**: Central processing unit.
- **Camera**: For waste image capture and identification.
- **Phidget Sensors**: For proximity detection, touch interaction, lid blockage detection and fill-level monitoring.
- **Servo Motors**: For automatic lid operation.
- **Microphone and Speaker**: For voice interaction and responses.

## Hardware Requirements

The following components are required to build and operate Binny:

- Raspberry Pi 4 (2GB or higher is recommended)
- Camera for image recognition (e.g., Raspberry Pi Camera Module, USB webcam)
- Phidget sensor kit ([distance](https://www.phidgets.com/?prodid=1171), [touch](https://www.phidgets.com/?prodid=1063), [force](https://www.phidgets.com/?prodid=76), and [infrared](https://www.phidgets.com/?prodid=1047) sensors)
- [Servo motor](https://www.phidgets.com/?prodid=150) and [phidegt control module](https://www.phidgets.com/?prodid=1044)
- [LCD screen](http://www.lcdwiki.com/3.5inch_RPi_Display) for user interface
- Microphone and speaker for voice interaction
- Plastic bin as the main structure

## Setup and Installation

The project needs Java 17, Scala, SBT and Python to run.

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/ZeTioZ/Binny.git
   ```
2. Create a virtual environment for the Python dependencies:
   ```bash
   python -m venv venv
   source venv/bin/activate # On Windows, use venv\Scripts\activate
   ```
3. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the Scala project:
   ```bash
   sbt "~run"
   ```
5. Start the servers for the AI and voice interaction:
   ```bash
   python ./src/main/python/tts-rest-server/rest_api.py
   python ./src/main/python/glados-tts/engine.py
   python ./src/main/python/llm/llm_hook.py
   ```

You may need to adjust the ip addresses in the python files to match your local network.
**For the AI to work, you need to have a llm server running on your network. Checkout [LMStudio](https://lmstudio.ai/) for an easy way to do this.**

## Possible Improvements

Binny is a work in progress, with several potential enhancements:
- Adding keyword activation for voice interaction.
- Improving object recognition accuracy with additional AI training.
- Optimizing hardware for faster response times.
- Introducing mobile app integration for bin management.
- Upgrading the visual interface with a larger touchscreen display.

## Contributors

- **Donato Gentile**
- **Diego Alarcon**
- **Matteo Devigne**
- **Arthur Barbieux**
- **Rodrigue Yando Djamen**

## License

This project is licensed under the [MIT License](LICENSE).

---

We hope Binny inspires fun and sustainable recycling habits. Contributions and suggestions are welcome!
