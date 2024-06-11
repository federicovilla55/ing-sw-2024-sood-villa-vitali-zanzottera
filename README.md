# ing-sw-2024-sood-villa-vitali-zanzottera

<div align="center">
<img src="https://www.craniocreations.it/storage/media/products/19/41/Codex_scatola+ombra.png" alt="Codex Naturalis" width="500" class="jop-noMdConv">
  
Digital version of the board game *Codex Naturalis*.
</div>

## Project

Final Project of Software Engineering at Polytechnic University of Milan. A.Y. 2023/2024. Prof. Cugola Gianpaolo Saverio.

### Team GC19

- Federico Villa (10768872): federico5.villa@mail.polimi.it
- Aryan Sood (10847706): aryan.sood@mail.polimi.it
- Matteo Vitali (10800443): matteo7.vitali@mail.polimi.it
- Marco Zanzottera (10765812): marco4.zanzottera@mail.polimi.it

# Implemnted functionalities
Those are the functionalities we have implmented:

<div align="center">
  
| Functionality                | State |
|:-----------------------------|:-----:|
| Basic rules                  |  :v:  |
| Complete rules               |  :v:  |
| TUI                          |  :v:  |
| GUI                          |  :v:  |
| RMI+Socket                   |  :v:  |
| Multiple games               |  :v:  |
| Persistence                  |  :x:  |
| Resilience to Disconnections |  :v:  |
| Chat                         |  :v:  |

</div>

# Software Requirements

- **OS**: Windows, MacOS, Linux
- **Java Runtime Environment** version 21

# How to run from JAR

## Server
- Download latest server jar from [releases](https://github.com/federicovilla55/ing-sw-2024-sood-villa-vitali-zanzottera/releases/latest/)
- Run in terminal: `java -jar GC19-server.jar`
- Set server IP and ports for Socket and RMI connections

## Client
- Download latest client jar from [releases](https://github.com/federicovilla55/ing-sw-2024-sood-villa-vitali-zanzottera/releases/latest/)
- Run in terminal: `java -jar GC19-client.jar`
- Set server IP and ports for Socket and RMI connections
- Select TUI or GUI (if on Windows and using TUI, see below)

### TUI on Windows Terminal
TUI game uses UTF-8 emoji characters in terminal,
supported by default on MacOS and Linux.
To enable emojis in Windows, follow these instructions:

- Get the new Windows Terminal. It has full support for Unicode and UTF-8
- Enable the new UTF-8 option in Windows settings. Go to the language settings, click Administrative language settings, then Change system locale… and tick the Beta: Use Unicode UTF-8 for worldwide language support option
- Restart your computer

# How to run from IDE (IntelliJ IDEA)

## Server
- Run `ServerApp`
- Set server IP and ports for Socket and RMI connections


## Client
- Run `ClientApp`
- Set server IP and ports for Socket and RMI connections


