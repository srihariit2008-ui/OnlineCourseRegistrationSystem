"""
Database Connection Module for Online Course Registration System.
Connects Python Data Science environment directly to MySQL.
Syllabus Reference: Foundations of Data Science (25ITC31) & DBMS (25ITT31).
"""

import os

def load_db_config():
    """
    Parses db.properties or environment variables to prevent hardcoded credentials.
    """
    config = {
        'host': 'localhost',
        'port': 3306,
        'user': 'root',
        'password': 'root',
        'database': 'online_course_registration'
    }

    # Check db.properties in expected locations
    prop_paths = [
        os.path.join(os.path.dirname(__file__), '..', 'java', 'src', 'db.properties'),
        os.path.join(os.path.dirname(__file__), 'db.properties'),
        'db.properties'
    ]

    for p in prop_paths:
        if os.path.exists(p):
            try:
                with open(p, 'r') as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith('#') and '=' in line:
                            k, v = line.split('=', 1)
                            k = k.strip()
                            v = v.strip()
                            if k == 'db.user': config['user'] = v
                            elif k == 'db.password': config['password'] = v
                            elif k == 'db.url':
                                # Parse jdbc:mysql://localhost:3306/online_course_registration
                                if '://' in v:
                                    part = v.split('://', 1)[1].split('?', 1)[0]
                                    host_port, db = part.split('/', 1)
                                    config['database'] = db
                                    if ':' in host_port:
                                        h, pt = host_port.split(':', 1)
                                        config['host'] = h
                                        config['port'] = int(pt)
                                    else:
                                        config['host'] = host_port
                break
            except Exception as e:
                print(f"[WARN] Failed to parse db.properties: {e}")

    # Environment variable overrides
    if os.getenv('DB_HOST'): config['host'] = os.getenv('DB_HOST')
    if os.getenv('DB_PORT'): config['port'] = int(os.getenv('DB_PORT'))
    if os.getenv('DB_USER'): config['user'] = os.getenv('DB_USER')
    if os.getenv('DB_PASSWORD'): config['password'] = os.getenv('DB_PASSWORD')
    if os.getenv('DB_NAME'): config['database'] = os.getenv('DB_NAME')

    return config

def get_connection():
    """
    Attempts to establish and return a live connection to MySQL.
    Returns: (conn, error_str)
    """
    config = load_db_config()
    try:
        import mysql.connector
        from mysql.connector import Error
    except ImportError:
        return None, "mysql-connector-python is not installed. Install via: pip install mysql-connector-python"

    try:
        conn = mysql.connector.connect(
            host=config['host'],
            port=config['port'],
            user=config['user'],
            password=config['password'],
            database=config['database']
        )
        if conn.is_connected():
            return conn, None
        else:
            return None, "Connection failed to establish."
    except Error as e:
        return None, f"MySQL Error [{e.errno}]: {e.msg}"
    except Exception as e:
        return None, str(e)

if __name__ == '__main__':
    print("Testing MySQL Database Connection...")
    c, err = get_connection()
    if c:
        print("[SUCCESS] Connected to MySQL database successfully!")
        c.close()
    else:
        print(f"[OFFLINE/ERROR] {err}")
