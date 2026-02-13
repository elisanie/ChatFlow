import csv
import matplotlib.pyplot as plt

records = []
with open('results.csv', 'r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        records.append(float(row['relative_start_ms']))

min_time = min(records)
bucket_size = 1000
buckets = {}
for t in records:
    bucket = int((t - min_time) // bucket_size) * 1
    buckets[bucket] = buckets.get(bucket, 0) + 1

x = sorted(buckets.keys())
y = [buckets[b] / 1.0 for b in x]

plt.figure(figsize=(10, 5))
plt.plot(x, y, marker='o')
plt.xlabel('Time (seconds)')
plt.ylabel('Throughput (messages/sec)')
plt.title('Throughput Over Time (1-second buckets)')
plt.grid(True)
plt.tight_layout()
plt.savefig('throughput.png')
print("Chart saved as throughput.png")
