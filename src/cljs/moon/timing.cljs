(ns moon.timing) 


(defn ->minutes [seconds]
  (vector (int (/ seconds 60)) (rem seconds 60)))

(defn ->hours [seconds]
  (into [] (if (< seconds 3600)
             (cons 0 (->minutes seconds))
             (cons (int (/ seconds 3600)) (->minutes (rem seconds 3600)))))) 
  
