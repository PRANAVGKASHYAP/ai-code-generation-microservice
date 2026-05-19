# Stage 1: Build the Vite application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package metadata
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci || npm install

# Copy application source code
COPY . .

# Set VITE_API_URL environment variable for build time embedding
ARG VITE_API_URL=http://api.kashyapcode.in
ENV VITE_API_URL=$VITE_API_URL

# Build the React application
RUN npm run build

# Stage 2: Serve the application via Nginx
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Create custom Nginx configuration inline to handle React Router
RUN echo 'server {' > /etc/nginx/conf.d/default.conf && \
    echo '    listen 80;' >> /etc/nginx/conf.d/default.conf && \
    echo '    server_name _;' >> /etc/nginx/conf.d/default.conf && \
    echo '    root /usr/share/nginx/html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    index index.html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    # React Router fallback' >> /etc/nginx/conf.d/default.conf && \
    echo '    location / {' >> /etc/nginx/conf.d/default.conf && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '}' >> /etc/nginx/conf.d/default.conf

# Copy compiled build output from Stage 1
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 for traffic
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]
