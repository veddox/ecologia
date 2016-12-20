;;;;
;;;; ecophyl is a phylogenic analysis tool for Ecologia that keeps track of
;;;; every animal in a run and enables lines of descent to be studied.
;;;; This file contains the main Common Lisp package for ecophyl.
;;;;
;;;; Copyright (c) 2016 Daniel Vedder
;;;; Licensed under the terms of the GPLv3.
;;;;

;;; ECOLOGIA VERSION 1.1

;;; Define the package

(defpackage "ECOPHYL"
	(:use "COMMON-LISP")
	(:export "GET-ANIMAL" "ANALYSE-LOG" "SAVE-ANIMALS" "LOAD-ANIMALS"
		"ANCESTORS" "OFFSPRING" "LCA" "LATEST-COMMON-ANCESTORS"
		"ANIMAL" "GENOME" "*ANIMALS*")) 

(in-package "ECOPHYL")


;;; Define needed variables and structs

(defstruct animal
	(id 0)
	(species NIL)
	(parent 0)
	(generation 0)
	(offspring NIL)
	(born 0)
	(age -1)
	(genome NIL))

(defstruct genome
	(mutation-rate 0)
	(speed 0)
	(stamina 0)
	(sight 0)
	(metabolism 0)
	(age-limit 0)
	(strength 0)
	(reproductive-energy 0)
	(maturity-age 0)
	(gestation 0)
	(reproduction-rate 0))

;;Create the list of animals with a generic "ancestor"
(defvar *animals* (list (make-animal)))


;;; Ecologia-related functions
;; I/O functions

(defun get-animal (id)
	"Return the animal with this ID number"
	(dolist (a *animals*)
		(when (= (animal-id a) id)
			(return a))))

(defun analyse-log (logfile)
	"Read in a log file and extract all animal data"
	;;XXX This involves a lot of very precise string surgery.
	;; Any change in the log format (as regards analysis() calls) is
	;; likely to lead to breakage here!
	;;FIXME Try to remove the remaining 'surgery' bits
	(do* ((log (load-text-file logfile)) (ln (length log))
			 (i 0 (1+ i)) (line (nth i log) (if (= i ln) "" (nth i log)))
			 (words (split-string line #\space) (split-string line #\space)))
		((= i ln) (format t "~&Done."))
		(when (member "ANALYSIS:" words :test #'equalp)
			(format t "~&Analysing line ~S." i)
			(cond ((member "created" words :test #'equalp)
					  (let* ((parent (get-animal (get-property "parent" words)))
								(id (get-property "ID"))
								(a (make-animal :id id
									   :species (nth 5 words)
									   :parent (animal-id parent)
									   :generation (get-property "generation")
									   :born (get-property "update"))))
						  (setf *animals* (append *animals* (list a)))
						  (setf (animal-offspring parent)
							  (append (animal-offspring parent) (list id)))))
				((member "genome" words :test #'equalp)
					(let ((id (read-from-string
								  (first (cut-string (nth 7 words)
											 (1- (length (nth 7 words)))))))
							 (g (make-genome
									:age-limit (get-property "ageLimit" words)
									:maturity-age (get-property "maturityAge")
									:strength (get-property "strength")
									:reproductive-energy (get-property "reproductiveEnergy")
									:stamina (get-property "stamina")
									:sight (get-property "sight")
									:mutation-rate (get-property "mutationRate")
									:metabolism (get-property "metabolism")
									:reproduction-rate (get-property "reproductionRate")
									:gestation (get-property "gestation")
									:speed (get-property "speed"))))
						(setf (animal-genome (get-animal id)) g)))
				((member "died" words :test #'equalp)
					(let ((id (read-from-string (nth 5 words)))
							 (age (read-from-string (nth 9 words))))
						(setf (animal-age (get-animal id)) age)))))))

(let ((plist))
	(defun get-property (prop &optional prop-list)
		"Extract the value of prop from a list of strings of the form 'x=y'"
		;; A helper function for analyse-log
		;; The property list is cached for easier syntax/better performance
		(if prop-list (setf plist prop-list)
			(unless plist (error "get-property: No property list specified!")))
		(dolist (elt plist)
			(let ((pv (split-string elt #\=)))
				(when (equalp prop (first pv))
					(return-from get-property (read-from-string (second pv))))))))

(defun save-animals (filename)
	"Save a list of animals to file"
	(with-open-file (f filename :direction :output)
		(format f "~S" *animals*)))

(defun load-animals (filename)
	"Load a list of animals previously saved to file"
	(with-open-file (f filename) ;;XXX What if the file doesn't exist?
		(setf *animals* (read f))))

;; Analysis functions

(defun ancestors (id)
	"Find the ancestors of the given animal"
	(if (zerop id) NIL
		(cons id (ancestors (animal-parent (get-animal id))))))

(defun offspring (id)
	"Find all offspring of the given animal"
	(let* ((animal (get-animal id)) (offspring (animal-offspring animal)))
		(dolist (c (animal-offspring animal) offspring)
			;;XXX This doesn't order the list, but who cares
			(setf offspring (append offspring (offspring c))))))

;; XXX This is *very* time intensive!
(defun latest-common-ancestors (alist)
	"Find the latest common ancestors of a list of animals"
	;; More specifically, return the shortest list of animal IDs who
	;; together are ancestral to all inputed animals
	(let ((mca (lca alist)) (next-list nil))
		(setf next-list
			(remove-if #'(lambda (a) (member mca (ancestors a))) alist))
		(if (null next-list) (list mca)
			(cons mca (latest-common-ancestors next-list)))))

(defun lca (alist)
	"Find the most frequent latest common ancestor of a list of animal IDs"
	;; This returns multiple values: the ID of the LCA and its frequency
	(do ((ancestor-list (mapcar #'(lambda (x) (reverse (ancestors x))) alist))
			(mca -1) (freq 0) (next-mca -1) (next-freq 0) (i 0 (1+ i)))
		((or (null next-mca) (< next-freq freq)) (values mca freq))
		(multiple-value-setq (next-mca next-freq)
			(most-common-element (nths i ancestor-list)))
		(when (and (> next-freq freq) next-mca)
			(setf mca next-mca freq next-freq))))

;;; Utility functions

;; XXX Not all of these are going to be needed, I just copied them en bloc
;; from my standard util.lisp file...

;; XXX Copy the whole util.lisp file here and use it as its own package?

(defmacro cassoc (entry table &key (test #'eql))
	"Returns (car (cdr (assoc entry table)))"
	`(car (cdr (assoc ,entry ,table :test ,test))))
				
(defun load-text-file (file-name)
	"Load a text file into a list of strings (representing the lines)"
	(with-open-file (f file-name)
		(do* ((line (read-line f nil nil)
				  (read-line f nil nil))
				 (file-lines (list line) (append file-lines (list line))))
			((null line) file-lines))))

(defun string-from-list (lst &optional (separator " - "))
	"Put all elements of lst into a single string, separated by the separator"
	(cond ((null lst) "")
		((= (length lst) 1) (to-string (car lst)))
		(T (concatenate 'string (to-string (first lst)) (to-string separator)
			(string-from-list (cdr lst) separator)))))

(defun split-string (str separator)
	"Split the string up into a list of strings along the separator character"
	(cond ((equalp str (to-string separator)) NIL)
		((zerop (count-instances separator str)) (list str))
		(T (let ((split-elt (cut-string str (position separator str))))
			   (cons (first split-elt)
				   (split-string (second (cut-string (second split-elt) 1))
					   separator))))))

(defun cut-string (s i)
	"Cut string s in two at index i and return the two substrings in a list"
	(if (or (minusp i) (> i (length s))) s
		(let ((s1 (make-string i)) (s2 (make-string (- (length s) i))))
			(dotimes (c (length s) (list s1 s2))
				(if (> i c)
					(setf (aref s1 c) (aref s c))
					(setf (aref s2 (- c i)) (aref s c)))))))

(defun char-list-to-string (char-list)
	"Convert a character list to a string"
	(let ((s (make-string (length char-list) :initial-element #\SPACE)))
		(dotimes (i (length char-list) s)
			(setf (aref s i) (nth i char-list)))))

(defun trim-whitespace (s)
	"Trim off spaces and tabs before and after string s"
	(string-trim '(#\space #\tab) s))

(defun to-string (x)
	"Whatever x is, convert it into a string"
	(cond ((stringp x) x)
		((or (symbolp x) (characterp x)) (string x))
		(t (format NIL "~S" x))))

(defun extract-elements (str)
	"Extract all Lisp elements (strings, symbols, numbers, etc.) from str"
	(multiple-value-bind (next-element i) (read-from-string str nil)
		(if (null next-element) NIL
			(cons next-element
				(extract-elements (second (cut-string str i)))))))

(defun count-instances (search-term search-sequence &key (test #'eql))
	"Count the number of instances of search-term in search-sequence"
	(let ((count 0))
		(dotimes (i (length search-sequence) count)
			(when (funcall test search-term (elt search-sequence i))
				(incf count)))))

(defun most-common-element (lst &key (test #'eql))
	"Return the most common element in this list and how often it appears"
	;;This function has multiple return values!
	;;In case of multiple mces, return the one that appears first
	(let ((elements-counted NIL) (max 0) (mce NIL))
		(dolist (e lst (values mce max))
			(unless (member e elements-counted :test test)
				(let ((count (count-instances e lst :test test)))
					(when (> count max)
						(setf max count)
						(setf mce e)))
				(setf elements-counted (append elements-counted (list e)))))))

(defun nths (n lst)
	"Take in a list of lists and return the nth element of each"
	(when (and lst (listp (car lst)))
		(cons (nth n (car lst)) (nths n (cdr lst)))))

(defun range (stop &key (start 0) (step 1))
	"Return a list of numbers from start to stop"
	;;XXX Surely this must exist as a function in Common Lisp already,
	;; I just don't know what it's called...
	(unless (>= start stop)
		(cons start (range stop :start (+ start step) :step step))))
		
;;; For acceptable performance during analysis,
;;; some functions need to be compiled
(dolist (fn '(analyse-log get-property split-string
				 cut-string count-instances))
	(compile fn))
