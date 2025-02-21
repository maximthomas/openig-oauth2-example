FROM node:18-alpine

WORKDIR /app

COPY src .

ENTRYPOINT [ "npx", "http-server", ".", "-c-1"  ]