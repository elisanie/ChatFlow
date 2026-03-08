
## ALB Configuration
- Load Balancer: chatflow-alb
- DNS: chatflow-alb-308077375.us-west-2.elb.amazonaws.com
- Listener: HTTP:80 → chatflow-tg
- Algorithm: Round Robin
- Health check: /health, interval 30s

## Instance Types
- server-v2: t2.micro x4
- rabbitmq-ec2: t2.micro x1
- consumer-ec2: t2.micro x1