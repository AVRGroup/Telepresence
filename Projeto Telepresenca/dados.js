var io = require('socket.io')(http);var app = require('express')();
var http = require('http').Server(app);
var serialport = require('serialport');// include the library
var io = require('socket.io')(http);

   var myPort = new serialport("COM6", { //ESCOLHER A PORTA
   baudRate: 9600,
   // look for return and newline at the end of each data packet:
   parser: serialport.parsers.readline("\n")
 });
 myPort.open; //Inicia conexao com a porta serial e exibe informações sobre a conexao
 myPort.on('open', showPortOpen);
 function showPortOpen() {
   console.log('port open. Data rate: ' + myPort.options.baudRate);
}

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

io.sockets.on('connection', function(socket) {
  socket.on('new message', function(data) {
	//console.log(data);
    var orientation = data[0] + data[1] + data[2] + data[8] + data[9] + data[10];
	console.log(orientation);
	//console.log("info" + data[16]+data[17]+data[18]);
	myPort.write(orientation);
	
  });
});

http.listen(3000, function(){
  console.log('listening on *:3000');
});