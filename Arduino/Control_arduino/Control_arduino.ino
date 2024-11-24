#include <SoftwareSerial.h>

SoftwareSerial miBT(10, 11);
char DATO = 0;
int LEDROJO = 44;
int LEDVERDE = 42;

void setup(){
  Serial.begin(9600);
  miBT.begin(38400);
  pinMode(LEDROJO, OUTPUT);
  pinMode(LEDVERDE, OUTPUT);
}

void loop(){
  if (miBT.available()){
    DATO = miBT.read();
    Serial.println(DATO);

    if (DATO == '1'){
      digitalWrite(LEDROJO, HIGH);
    } else if (DATO == '2'){
      digitalWrite(LEDROJO, LOW);
    }

    if (DATO == 'C'){
      digitalWrite(LEDVERDE, HIGH);
    } else if (DATO == 'D'){
      digitalWrite(LEDVERDE, LOW);
    }

  }
}

