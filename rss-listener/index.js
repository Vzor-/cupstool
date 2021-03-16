const http = require('http');

const server = http.createServer((req, res) => {

  let body = '';
  req.setEncoding('utf8');

  req.on('data', (chunk) => {
    body += chunk;
  });

  req.on('end', () => {
    try {
        console.log(body);
      res.statusCode = 200;
      res.write(`ok`);
      res.end();
    } catch (er) {
      console.log("Malformed");
      res.statusCode = 400;
      return res.end(`error: ${er.message}`);
    }
    body = "";
  });
});

server.listen(8000);
console.log("Server started.");