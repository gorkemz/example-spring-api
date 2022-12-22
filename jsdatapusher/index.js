const http = require('http');

function sendData(data, callback) {
	const options = {
		hostname: 'localhost',
		port: 8080,
		path: '/data',
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
	};

	const req = http.request(options, (res) => {
		res.on('data', (chunk) => {
			callback(chunk);
		});
	});

	req.write(JSON.stringify(data));
	req.end();
}

function CreateData() {

	return {
		timestamp: Math.floor(Date.now() / 1000),
		macId: Math.floor(Math.random() * 90 + 10) + ':' + Math.floor(Math.random() * 90 + 10) + ':' + Math.floor(Math.random() * 90 + 10) + ':' + Math.floor(Math.random() * 90 + 10) + ':' + Math.floor(Math.random() * 90 + 10),
		in: Math.floor(Math.random() * 11),
		out: Math.floor(Math.random() * 11),
	};

}


const hostname = '127.0.0.1';
const port = 3000;

const server = http.createServer((req, res) => {
	res.writeHead(200, {
		'Content-Type': 'text/html',
		'Cache-Control': 'no-cache',
		'Connection': 'keep-alive'
	});


	setInterval(() => {
		data = CreateData();
		sendData(data, (response) => {
			console.log(response.toString('utf-8'));
			res.write(`${response.toString('utf-8')}`);
      res.write("<br>");
		});
	}, 5000);

});


server.listen(port, hostname, () => {
	console.log(`Server running at http://${hostname}:${port}/`);
});