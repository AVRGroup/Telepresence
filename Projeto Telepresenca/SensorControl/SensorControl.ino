
#include <Servo.h> //Biblioteca Servo
#include <time.h>
Servo motor1;
Servo motor2;//variavel servo
int angulo1,angulo2; 
int i=0,val[6];

void setup(){
  Serial.begin(9600);//frequência da porta serial
  motor1.attach(6);//Motor ligado no pino 6
  motor2.attach(7);
  angulo1=90; //angulo inicial do motor
  angulo2=90;
  motor1.write(angulo1);
  motor2.write(angulo2);
  i=0;
}

 
void loop(){
  if(Serial.available() > 0){ //verifica se existe comunicação com a porta seria)
    if(i==5){
      val[i] = Serial.read()-48; //Armazena o valor no vetor convertendo ascii->int
      //Divindo o vetor e convertendo para o valor do angulo esperado//
      angulo1 = 100*(val[0]-1)+10*val[1]+val[2];
      angulo2= 100*(val[3]-1)+10*val[4]+val[5];
      if(angulo1>180)angulo1=180;
      //Movimentando os Motores//
     motor1.write(abs(180-angulo1)); 
     motor2.write(angulo2);
      i = -1; //Altera o contador para iniciar outro angulo
     
    }else{
      val[i] = Serial.read()-48; //Armazena o valor no vetor convertendo ascii->int
    }
    i++;
  }
}

