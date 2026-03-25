# Monitoring

## RabbitMQ Management
- URL: http://<rabbitmq-ec2-ip>:15672
- Metrics: queue depth, publish/consume rate, connections

## Database Metrics
- Tool: MySQL CLI / TablePlus
- Key metrics: total messages, active users, top rooms
- Query via Metrics API: http://<alb-dns>/metrics

## Key Parameters
- Prefetch count: 50
- Consumer threads: 8
- Batch size: 500
- Flush interval: 500ms
- HikariCP max connections: 20
- Queue count: 20
