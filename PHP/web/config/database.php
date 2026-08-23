<?php

class Database {
    private static $instance = null;
    private $conn;

    private $host;
    private $user;
    private $pass;
    private $dbname;

    private function __construct() {
        $dbUrl = getenv('DB_URL');
        if ($dbUrl && preg_match('/\/\/([^:\/]+)/', $dbUrl, $m)) {
            $this->host = $m[1];
        } else {
            $this->host = $dbUrl ?: 'localhost';
        }
        $this->user = getenv('DB_USER') ?: 'root';
        $this->pass = getenv('DB_PASSWORD') ?: '';
        $this->dbname = 'jfin';

        try {
            $this->conn = new PDO(
                "mysql:host={$this->host};dbname={$this->dbname};charset=utf8mb4",
                $this->user,
                $this->pass,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false,
                ]
            );
        } catch (PDOException $e) {
            die("Erro de conexão: " . $e->getMessage());
        }
    }

    public static function getInstance() {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    public function getConnection() {
        return $this->conn;
    }

    public static function prepare($sql) {
        return self::getInstance()->getConnection()->prepare($sql);
    }

    public static function query($sql) {
        return self::getInstance()->getConnection()->query($sql);
    }

    public static function lastInsertId() {
        return self::getInstance()->getConnection()->lastInsertId();
    }
}
