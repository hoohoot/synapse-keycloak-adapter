FROM alpine:edge

RUN apk add --no-cache \
		ca-certificates \
		bash \
		nginx \
		tar \
		tzdata \
		wget \
	&& cp /usr/share/zoneinfo/Europe/Paris /etc/localtime \
	&& apk del --no-cache tzdata \
	&& mkdir -p /opt/riot \
	&& adduser -D -h /opt/riot riot \
	&& chown -R riot /opt/riot

ENV RIOT_VERSION 0.16.3

USER riot
WORKDIR /opt/riot
RUN wget -q --show-progress "https://github.com/vector-im/riot-web/releases/download/v$RIOT_VERSION/riot-v$RIOT_VERSION.tar.gz" \
	&& tar xf "riot-v$RIOT_VERSION.tar.gz" \
	&& rm "riot-v$RIOT_VERSION.tar.gz"
WORKDIR /opt/riot/riot-v$RIOT_VERSION
ADD config.json .

EXPOSE 8009
USER root

ADD nginx.conf /etc/nginx/nginx.conf
RUN sed -i "s,RIOT_VERSION,$RIOT_VERSION,g" /etc/nginx/nginx.conf

CMD nginx -g "daemon off;"
