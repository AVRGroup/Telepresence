

http = require('http');
fs = require('fs');
var serialport = require('serialport');// include the library
  // SerialPort = serialport.SerialPort; // make a local instance of it
var orientation;
   
   var myPort = new serialport("COM3", { //ESCOLHER A PORTA
   baudRate: 9600,
   // look for return and newline at the end of each data packet:
   parser: serialport.parsers.readline("\n")
 });
 myPort.open; //Inicia conexao com a porta serial e exibe informações sobre a conexao
 myPort.on('open', showPortOpen);
 function showPortOpen() {
   console.log('port open. Data rate: ' + myPort.options.baudRate);
}


server = http.createServer( function(req, res) {
	
    console.dir(req.param);

    if (req.method == 'POST') {
        console.log("POST");
        var body = '';
        req.on('data', function (data) {
            body += data;
            console.log("Partial body: " + body);
        });
        req.on('end', function () {
            console.log("Body: " + body);
			orientation=body[0]+body[1]+body[2]+body[8]+body[9]+body[10]; //SEPARANDO CARACTERES IMPORTANTES
			//console.log(orientation); //VERIFICANDO ORIENTAÇÃO RECEBIDA COM O TERMINAL
			myPort.write(orientation); //ENVIANDO PARA A PORTA
        });
       // res.writeHead(200, {'Content-Type': 'text/html'});
        res.end('post received');
    }
    else
    {
        console.log("GET");
        //var html = '<html><body><form method="post" action="http://localhost:3000">Name: <input type="text" name="name" /><input type="submit" value="Submit" /></form></body>';
        var html = fs.readFileSync('index.html');
        res.writeHead(200, {'Content-Type': 'text/html'});
        res.end(html);
    }
	 

});

port = 3000;
host = '10.5.23.193';
server.listen(port, host);
console.log('Listening at http://' + host + ':' + port);