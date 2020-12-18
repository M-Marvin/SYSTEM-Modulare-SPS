# SYSTEM-Modulare-SPS
Eine Modulares SPS System aus einem Raspberry Pi und ATmega328 Microcontrolern

Das Projekt ist noch nicht fertig, aber auf einem Steckbrett hat es schon funktioniert!

WISCHITG: Datei wird auf der GitHub Webseite verbuggt angezeigt, besser runterladen und mit standart Texteditor öffnen!

-----------------------------------------------------------------------------------------------------

Die Modulare SPS besteht aus einem Raspberry Pi. der über ein Adaptermodul und
einem Flachbandkabel mit den einzelnen Modulen verbunden wird.

Die Moduel bestehen aus auf Hut-Schienen anbringbaren 3D-Gedruckten Gehäusen mit
entsprechenden Schaltungen darin.

Bisherige Module:
  - keine

In Entwiklung:
  - DO_16_R230  16 x Digitalausgang mit 230V Relay-Wechlerkontakten

Geplante Module:
  - DI_16_D24   16 x Digitaleingang 24V direkt gekoppelt (keine Relays dazwischen)
  - AI_1_D0-24  1 x Analogeingang 0V-24V direkt gekoppelt
  - AO_1_D0-24 1 x Analogausgang 0V-24V direkt gekoppelt (benötigt 24V Spannungsversorgung)

-----------------------------------------------------------------------------------------------------

Kurzbezeichnungs-Legende:

Beispiel: DO - 16 - R230

D = Digital (1 oder 0 / Ein oder Aus)
O = Output (Ausgang)

16 = Schnittstelle 16 mal vorhanden

R = Relay/Galvanisch getrennt
230 = Maximale Spannung 230V

Zusammensetzung: [Art der Schnittstelle] [Eingang/Ausgang*] _ [Anzahl der Schnittstellen] _ [Kopplungsart] [Maximal-Spannung/Spannungsbereich]
* (oder weggelassen wenn nicht klar definierbar, z.B: bei Netzwerk)

[Art der Schnittstelle]
- D Digital
- A Analog
- B Bus-System (zu Zeit noch nicht verwended, z.B. Netzwerk, D-Sub, MPI, ISP, USB, usw.)

[Eingang/Ausgang]
- I Eingang
- O Ausgang

[Kopplungsart]
- R Relay/Galvanisch getrennt
- D Direkt gekoppelt (GND der Leiterplatte/SPS und GND der Externen Anschlüsse sind identisch)

-----------------------------------------------------------------------------------------------------

Das projekt besteht aus:
- Raspberry Pi SPS Hauptprogramm (Dort wird das Programm der Steuerung und die Einstellung der Module reingeladen)
- ATmega Modul-Programme (Programme der jeweiligen Module)
- Schaltpläne/Leiterplatten entwürfe
- 3D-Druck Modelle für Gehäuse/Halterungen

Das Raspberry Pi Programm läuft mit der Bibliothek pi4j auf jedem kompatiblen Raspebrry Pi.
Die Datei wird einfach auf das Pi kopiert und über einen der folgenden Befehle gestartet:
    sudo java -jar *Pfad der Jar*               um das SPS Programm zu starten
    sudo java -jar *Pfad der Jar* -configMode   um den System-Konfigurator zu starten und die Adressen der Module fest zu legen

Die ATmega Programme werden als .hex (Textdatei mit Hexadezimal-Codes) compiliert und per SPI in den Controler geladen.

-----------------------------------------------------------------------------------------------------
