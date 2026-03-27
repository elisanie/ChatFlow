#!/bin/bash
while true; do
    echo "=== $(date) ===" >> db_metrics.log
    mysql -u chatapp -pwehavetoeat chatflow -e "
        SHOW GLOBAL STATUS WHERE Variable_name IN (
            'Questions',
            'Threads_connected',
            'Innodb_buffer_pool_reads',
            'Innodb_buffer_pool_read_requests',
            'Innodb_rows_inserted'
        );
    " >> db_metrics.log 2>&1
    sleep 30
done