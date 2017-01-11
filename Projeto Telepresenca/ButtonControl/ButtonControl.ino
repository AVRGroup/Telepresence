
#include <Servo.h> //Biblioteca Servo
#include <time.h>
Servo motor1;
Servo motor2;//variavel servo
int angulo1,angulo2; //angulo inicial do motor
int anguloBase;//Posicionamento Inicial
double unidadeBaseGiro;//Segundos por Grau
double tempoDeGiro;
int lixo,i=0,val[6];
String dado;
char command;


void setup(){
  Serial.begin(9600);//frequência da porta serial
  motor1.attach(6);//Motor ligado no pino 6
  motor2.attach(7);
  angulo1=90; //angulo inicial do motor
  angulo2=90;
  motor1.write(angulo1);
  motor2.write(angulo2);
  anguloBase=0;//Posicionamento Inicial
  unidadeBaseGiro=3.35;//Milisegundos por Grau
  tempoDeGiro=0;
  i=0;
}

/*int getInt(String texto)
{
int temp = texto.length() + 1;
char buffer[temp];
texto.toCharArray(buffer, temp);
int num = atoi(buffer);
return num;
}*/
 
void loop(){
  if(Serial.available() > 0){ //verifica se existe comunicação com a porta seria)
     command = Serial.read();
     if (command == 'w') 
     {
      if(angulo2<180)
      angulo2 += 2;
      motor2.write(angulo2);
     }
      if (command == 's') 
     {
      if(angulo2>0)
      angulo2 -= 2;
      motor2.write(angulo2);
     }
       if (command == 'a') 
     {
       if(angulo1<180)
        angulo1+=2;
        motor1.write(angulo1);
      
     }  if (command == 'd') 
     {
      if(angulo1>0)
        angulo1-=2;
        motor1.write(angulo1);
     }
     
  }
}

