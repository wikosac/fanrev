#include "DHT.h"
#include "LiquidCrystal_I2C.h"
#include <SoftwareSerial.h>

DHT dht;
LiquidCrystal_I2C lcd(0x27, 16, 2);

const int pirPin = 8;                // PIR sensor pin
const int trigPin = 7;               // Ultrasonic sensor trigger pin
const int echoPin = 6;               // Ultrasonic sensor echo pin
const float maxValidDistance = 150;  // Maximum valid distance (in cm)

unsigned long previousMillis = 0;
const long interval = 5000;  // Detection interval (in milliseconds)
int personCount = 0;
bool personInside = false;
bool fanOn = false;
float suhu = 0.0;
int delayTime = 2000;  // Waktu jeda antara perubahan kecepatan

const unsigned long lcdInterval = 500;
unsigned long lcdPreviousMillis = 0;

SoftwareSerial BTSerial(10, 11);

// MotorDriver
// Define the pins for motor control
#define IN3 5  // deklarasi pin IN3
#define IN4 4  // deklarasi pin IN4
#define ENB 3  // deklarasi pin ENB

void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);
  dht.setup(2);
  pinMode(pirPin, INPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  lcd.init();
  lcd.backlight();

  // MotorDriver
  // Set the pins as outputs
  // Konfigurasi pin-pin sebagai Output
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(ENB, OUTPUT);
}

void loop() {
  unsigned long currentMillis = millis();

  // Measure temperature and humidity
  float suhu = dht.getTemperature();
  float hum = dht.getHumidity();
  delay(dht.getMinimumSamplingPeriod());

  // Mengontrol kipas berdasarkan suhu dan deteksi gerakan
  if (fanOn) {
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 128);  // Mengatur kecepatan motor B (0-255)
  } else {
    // cek kondisi jika ada orang dan suhu diatas 29
    if (personInside && suhu >= 28.0 && suhu <= 29.0) {  // jika suhu 28-29 speed1
      fanOn = true;
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite(ENB, 128);  // Mengatur kecepatan motor B (0-255)
      delay(delayTime);
    } else if (personInside && suhu >= 30.0 && suhu <= 31.0) {  // jika suhu 30-31 speed2
      fanOn = true;
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite(ENB, 192);  // Mengatur kecepatan motor B (0-255)
      delay(delayTime);
    } else if (personInside && suhu >= 32.0) {  // jika suhu diatas 32 speed3
      fanOn = true;
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite(ENB, 255);  // Mengatur kecepatan motor B (0-255)
      delay(delayTime);
    } else {
      fanOn = false;
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, LOW);
      analogWrite(ENB, 0);  // Motor stops
    }
  }

  if (BTSerial.available()) {
    String readSerial = BTSerial.readStringUntil('\n');
    Serial.println("Received Bluetooth message: " + readSerial);  // Debugging statement
    // Process the command from Bluetooth
    processCommand(readSerial);
  }

  if (Serial.available()) {
    String command = Serial.readStringUntil('\n');
    // command.toLowerCase();

    // Process the command from Serial Monitor
    processCommand(command);
  }

  // Display temperature and humidity on LCD
  if (currentMillis - lcdPreviousMillis >= lcdInterval) {
    lcdPreviousMillis = currentMillis;

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Suhu : ");
    lcd.print(suhu);
    lcd.print(" C");
    lcd.setCursor(0, 1);
    lcd.print("Hum  : ");
    lcd.print(hum);
    lcd.print(" %");
  }

  // Detect human motion
  int pirDetected = digitalRead(pirPin);

  if (pirDetected == HIGH) {
    float distance = measureDistance();
    if (distance <= maxValidDistance) {
      if (!personInside) {
        personInside = true;
        personCount++;
        Serial.print("Person entered. Total count: ");
        Serial.println(personCount);
      }
      previousMillis = currentMillis;
    }
  } else {
    if (personInside && currentMillis - previousMillis >= interval) {
      personInside = false;
      Serial.println("Person exited.");
    }
  }
}

float measureDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  unsigned long duration = pulseIn(echoPin, HIGH);
  float distance = (duration * 0.0343) / 2;  // Calculate distance in cm

  return distance;
}

void processCommand(String command) {
  // Convert the command to lowercase for case-insensitive comparison
  command.trim();
  command.toLowerCase();


  if (command.equals("turn on")) {  // nyalakan kipas
    // Turn the fan on
    fanOn = true;
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 255);  // Mengatur kecepatan motor B (0-255)
    Serial.println("Fan turned on.");
  } else if (command.equals("turn off")) {  // nyalakan kipas
    // Turn the fan off
    fanOn = false;
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 0);  // Motor berhenti
    Serial.println("Fan turned off.");
  } else if (command.equals("suhu")) {  // menampilkan suhu di serial
    Serial.println(String(dht.getTemperature()) + " C");
    float temperature = dht.getTemperature();
    String messageSuhu = "Suhu: " + String(temperature) + " C";
    BTSerial.println(messageSuhu);
  } else if (command.equals("hum")) {  // menampilkan hum di serial
    Serial.println(String(dht.getHumidity()) + " %");
    float hum = dht.getHumidity();
    String messageHum = "Hum: " + String(hum) + " %";
    BTSerial.println(messageHum);
  } else if (command.equals("speed1")) {  // speed1 -> 50%
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 128);  // Mengatur kecepatan motor B (0-255)
    delay(delayTime);
    Serial.println("Fan 50%.");
  } else if (command.equals("speed2")) {  // speed2 -> 75%
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 192);  // Mengatur kecepatan motor B (0-255)
    delay(delayTime);
    Serial.println("Fan 75%.");
  } else if (command.equals("speed3")) {  // speed3 -> 100%
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
    analogWrite(ENB, 255);  // Mengatur kecepatan motor B (0-255)
    delay(delayTime);
    Serial.println("Fan 100%.");
  } else if (command.equals("check status")) {  // check if the fan is turned on or off
    if (fanOn) {
      Serial.println("Fan turned on.");
      BTSerial.println("Fan turned on");

      float temperature = dht.getTemperature();
      String messageSuhu = "Suhu: " + String(temperature) + " C";
      BTSerial.println(messageSuhu);

      float hum = dht.getHumidity();
      String messageHum = "Hum: " + String(hum) + " %";
      BTSerial.println(messageHum);
    } else {
      Serial.println("Fan turned off.");
      BTSerial.println("Fan turned off");

      float temperature = dht.getTemperature();
      String messageSuhu = "Suhu: " + String(temperature) + " C";
      BTSerial.println(messageSuhu);

      float hum = dht.getHumidity();
      String messageHum = "Hum: " + String(hum) + " %";
      BTSerial.println(messageHum);
    }
  }
}