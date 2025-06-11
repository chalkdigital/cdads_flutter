// Copyright 2011 Google Inc. All Rights Reserved.
(function() {
    var h, aa = "function" == typeof Object.create ? Object.create : function(a) {
            var b = function() {};
            b.prototype = a;
            return new b
        },
        ba;
    if ("function" == typeof Object.setPrototypeOf) ba = Object.setPrototypeOf;
    else {
        var ca;
        a: {
            var da = {
                    Ld: !0
                },
                ea = {};
            try {
                ea.__proto__ = da;
                ca = ea.Ld;
                break a
            } catch (a) {}
            ca = !1
        }
        ba = ca ? function(a, b) {
            a.__proto__ = b;
            if (a.__proto__ !== b) throw new TypeError(a + " is not extensible");
            return a
        } : null
    }
    var ha = ba,
        ia = function(a, b) {
            a.prototype = aa(b.prototype);
            a.prototype.constructor = a;
            if (ha) ha(a, b);
            else
                for (var c in b)
                    if ("prototype" != c)
                        if (Object.defineProperties) {
                            var d = Object.getOwnPropertyDescriptor(b, c);
                            d && Object.defineProperty(a, c, d)
                        } else a[c] = b[c];
            a.da = b.prototype
        },
        ka = "function" == typeof Object.defineProperties ? Object.defineProperty : function(a, b, c) {
            a != Array.prototype && a != Object.prototype && (a[b] = c.value)
        },
        la = "undefined" != typeof window && window === this ? this : "undefined" != typeof global && null != global ?
        global : this,
        ma = function() {
            ma = function() {};
            la.Symbol || (la.Symbol = na)
        },
        na = function() {
            var a = 0;
            return function(b) {
                return "jscomp_symbol_" + (b || "") + a++
            }
        }(),
        pa = function() {
            ma();
            var a = la.Symbol.iterator;
            a || (a = la.Symbol.iterator = la.Symbol("iterator"));
            "function" != typeof Array.prototype[a] && ka(Array.prototype, a, {
                configurable: !0,
                writable: !0,
                value: function() {
                    return oa(this)
                }
            });
            pa = function() {}
        },
        oa = function(a) {
            var b = 0;
            return qa(function() {
                return b < a.length ? {
                    done: !1,
                    value: a[b++]
                } : {
                    done: !0
                }
            })
        },
        qa = function(a) {
            pa();
            a = {
                next: a
            };
            a[la.Symbol.iterator] = function() {
                return this
            };
            return a
        },
        ra = function(a) {
            pa();
            var b = a[Symbol.iterator];
            return b ? b.call(a) : oa(a)
        },
        sa = function(a) {
            if (!(a instanceof Array)) {
                a = ra(a);
                for (var b, c = []; !(b = a.next()).done;) c.push(b.value);
                a = c
            }
            return a
        },
        ta = function(a, b) {
            if (b) {
                var c = la;
                a = a.split(".");
                for (var d = 0; d < a.length - 1; d++) {
                    var e = a[d];
                    e in c || (c[e] = {});
                    c = c[e]
                }
                a = a[a.length - 1];
                d = c[a];
                b = b(d);
                b != d && null != b && ka(c, a, {
                    configurable: !0,
                    writable: !0,
                    value: b
                })
            }
        };
    ta("Array.prototype.find", function(a) {
        return a ? a : function(a, c) {
            a: {
                var b = this;b instanceof String && (b = String(b));
                for (var e = b.length, f = 0; f < e; f++) {
                    var g = b[f];
                    if (a.call(c, g, f, b)) {
                        a = g;
                        break a
                    }
                }
                a = void 0
            }
            return a
        }
    });
    var wa = function(a, b) {
            return Object.prototype.hasOwnProperty.call(a, b)
        },
        xa = "function" == typeof Object.assign ? Object.assign : function(a, b) {
            for (var c = 1; c < arguments.length; c++) {
                var d = arguments[c];
                if (d)
                    for (var e in d) wa(d, e) && (a[e] = d[e])
            }
            return a
        };
    ta("Object.assign", function(a) {
        return a || xa
    });
    ta("Math.trunc", function(a) {
        return a ? a : function(a) {
            a = Number(a);
            if (isNaN(a) || Infinity === a || -Infinity === a || 0 === a) return a;
            var b = Math.floor(Math.abs(a));
            return 0 > a ? -b : b
        }
    });
    ta("Array.prototype.fill", function(a) {
        return a ? a : function(a, c, d) {
            var b = this.length || 0;
            0 > c && (c = Math.max(0, b + c));
            if (null == d || d > b) d = b;
            d = Number(d);
            0 > d && (d = Math.max(0, b + d));
            for (c = Number(c || 0); c < d; c++) this[c] = a;
            return this
        }
    });
    ta("WeakMap", function(a) {
        function b(a) {
            wa(a, d) || ka(a, d, {
                value: {}
            })
        }

        function c(a) {
            var c = Object[a];
            c && (Object[a] = function(a) {
                b(a);
                return c(a)
            })
        }
        if (function() {
                if (!a || !Object.seal) return !1;
                try {
                    var b = Object.seal({}),
                        c = Object.seal({}),
                        d = new a([
                            [b, 2],
                            [c, 3]
                        ]);
                    if (2 != d.get(b) || 3 != d.get(c)) return !1;
                    d["delete"](b);
                    d.set(c, 4);
                    return !d.has(b) && 4 == d.get(c)
                } catch (l) {
                    return !1
                }
            }()) return a;
        var d = "$jscomp_hidden_" + Math.random();
        c("freeze");
        c("preventExtensions");
        c("seal");
        var e = 0,
            f = function(a) {
                this.g = (e += Math.random() +
                    1).toString();
                if (a) {
                    ma();
                    pa();
                    a = ra(a);
                    for (var b; !(b = a.next()).done;) b = b.value, this.set(b[0], b[1])
                }
            };
        f.prototype.set = function(a, c) {
            b(a);
            if (!wa(a, d)) throw Error("WeakMap key fail: " + a);
            a[d][this.g] = c;
            return this
        };
        f.prototype.get = function(a) {
            return wa(a, d) ? a[d][this.g] : void 0
        };
        f.prototype.has = function(a) {
            return wa(a, d) && wa(a[d], this.g)
        };
        f.prototype["delete"] = function(a) {
            return wa(a, d) && wa(a[d], this.g) ? delete a[d][this.g] : !1
        };
        return f
    });
    ta("Map", function(a) {
        if (function() {
                if (!a || "function" != typeof a || !a.prototype.entries || "function" != typeof Object.seal) return !1;
                try {
                    var b = Object.seal({
                            x: 4
                        }),
                        c = new a(ra([
                            [b, "s"]
                        ]));
                    if ("s" != c.get(b) || 1 != c.size || c.get({
                            x: 4
                        }) || c.set({
                            x: 4
                        }, "t") != c || 2 != c.size) return !1;
                    var d = c.entries(),
                        e = d.next();
                    if (e.done || e.value[0] != b || "s" != e.value[1]) return !1;
                    e = d.next();
                    return e.done || 4 != e.value[0].x || "t" != e.value[1] || !d.next().done ? !1 : !0
                } catch (H) {
                    return !1
                }
            }()) return a;
        ma();
        pa();
        var b = new WeakMap,
            c = function(a) {
                this.h = {};
                this.g = f();
                this.size = 0;
                if (a) {
                    a = ra(a);
                    for (var b; !(b = a.next()).done;) b = b.value, this.set(b[0], b[1])
                }
            };
        c.prototype.set = function(a, b) {
            a = 0 === a ? 0 : a;
            var c = d(this, a);
            c.list || (c.list = this.h[c.id] = []);
            c.la ? c.la.value = b : (c.la = {
                next: this.g,
                Da: this.g.Da,
                head: this.g,
                key: a,
                value: b
            }, c.list.push(c.la), this.g.Da.next = c.la, this.g.Da = c.la, this.size++);
            return this
        };
        c.prototype["delete"] = function(a) {
            a = d(this, a);
            return a.la && a.list ? (a.list.splice(a.index, 1), a.list.length || delete this.h[a.id], a.la.Da.next = a.la.next, a.la.next.Da =
                a.la.Da, a.la.head = null, this.size--, !0) : !1
        };
        c.prototype.clear = function() {
            this.h = {};
            this.g = this.g.Da = f();
            this.size = 0
        };
        c.prototype.has = function(a) {
            return !!d(this, a).la
        };
        c.prototype.get = function(a) {
            return (a = d(this, a).la) && a.value
        };
        c.prototype.entries = function() {
            return e(this, function(a) {
                return [a.key, a.value]
            })
        };
        c.prototype.keys = function() {
            return e(this, function(a) {
                return a.key
            })
        };
        c.prototype.values = function() {
            return e(this, function(a) {
                return a.value
            })
        };
        c.prototype.forEach = function(a, b) {
            for (var c = this.entries(),
                    d; !(d = c.next()).done;) d = d.value, a.call(b, d[1], d[0], this)
        };
        c.prototype[Symbol.iterator] = c.prototype.entries;
        var d = function(a, c) {
                var d = c && typeof c;
                "object" == d || "function" == d ? b.has(c) ? d = b.get(c) : (d = "" + ++g, b.set(c, d)) : d = "p_" + c;
                var e = a.h[d];
                if (e && wa(a.h, d))
                    for (a = 0; a < e.length; a++) {
                        var f = e[a];
                        if (c !== c && f.key !== f.key || c === f.key) return {
                            id: d,
                            list: e,
                            index: a,
                            la: f
                        }
                    }
                return {
                    id: d,
                    list: e,
                    index: -1,
                    la: void 0
                }
            },
            e = function(a, b) {
                var c = a.g;
                return qa(function() {
                    if (c) {
                        for (; c.head != a.g;) c = c.Da;
                        for (; c.next != c.head;) return c =
                            c.next, {
                                done: !1,
                                value: b(c)
                            };
                        c = null
                    }
                    return {
                        done: !0,
                        value: void 0
                    }
                })
            },
            f = function() {
                var a = {};
                return a.Da = a.next = a.head = a
            },
            g = 0;
        return c
    });
    ta("Object.is", function(a) {
        return a ? a : function(a, c) {
            return a === c ? 0 !== a || 1 / a === 1 / c : a !== a && c !== c
        }
    });
    ta("Array.prototype.includes", function(a) {
        return a ? a : function(a, c) {
            var b = this;
            b instanceof String && (b = String(b));
            var e = b.length;
            c = c || 0;
            for (0 > c && (c = Math.max(c + e, 0)); c < e; c++) {
                var f = b[c];
                if (f === a || Object.is(f, a)) return !0
            }
            return !1
        }
    });
    ta("String.prototype.includes", function(a) {
        return a ? a : function(a, c) {
            if (null == this) throw new TypeError("The 'this' value for String.prototype.includes must not be null or undefined");
            if (a instanceof RegExp) throw new TypeError("First argument to String.prototype.includes must not be a regular expression");
            return -1 !== (this + "").indexOf(a, c || 0)
        }
    });
    ta("Promise", function(a) {
        function b() {
            this.g = null
        }

        function c(a) {
            return a instanceof e ? a : new e(function(b) {
                b(a)
            })
        }
        if (a) return a;
        b.prototype.h = function(a) {
            null == this.g && (this.g = [], this.o());
            this.g.push(a)
        };
        b.prototype.o = function() {
            var a = this;
            this.l(function() {
                a.A()
            })
        };
        var d = la.setTimeout;
        b.prototype.l = function(a) {
            d(a, 0)
        };
        b.prototype.A = function() {
            for (; this.g && this.g.length;) {
                var a = this.g;
                this.g = [];
                for (var b = 0; b < a.length; ++b) {
                    var c = a[b];
                    a[b] = null;
                    try {
                        c()
                    } catch (l) {
                        this.v(l)
                    }
                }
            }
            this.g = null
        };
        b.prototype.v =
            function(a) {
                this.l(function() {
                    throw a;
                })
            };
        var e = function(a) {
            this.h = 0;
            this.l = void 0;
            this.g = [];
            var b = this.o();
            try {
                a(b.resolve, b.reject)
            } catch (m) {
                b.reject(m)
            }
        };
        e.prototype.o = function() {
            function a(a) {
                return function(d) {
                    c || (c = !0, a.call(b, d))
                }
            }
            var b = this,
                c = !1;
            return {
                resolve: a(this.J),
                reject: a(this.v)
            }
        };
        e.prototype.J = function(a) {
            if (a === this) this.v(new TypeError("A Promise cannot resolve to itself"));
            else if (a instanceof e) this.D(a);
            else {
                a: switch (typeof a) {
                    case "object":
                        var b = null != a;
                        break a;
                    case "function":
                        b = !0;
                        break a;
                    default:
                        b = !1
                }
                b ? this.H(a) : this.A(a)
            }
        };
        e.prototype.H = function(a) {
            var b = void 0;
            try {
                b = a.then
            } catch (m) {
                this.v(m);
                return
            }
            "function" == typeof b ? this.F(b, a) : this.A(a)
        };
        e.prototype.v = function(a) {
            this.w(2, a)
        };
        e.prototype.A = function(a) {
            this.w(1, a)
        };
        e.prototype.w = function(a, b) {
            if (0 != this.h) throw Error("Cannot settle(" + a + ", " + b + "): Promise already settled in state" + this.h);
            this.h = a;
            this.l = b;
            this.B()
        };
        e.prototype.B = function() {
            if (null != this.g) {
                for (var a = 0; a < this.g.length; ++a) f.h(this.g[a]);
                this.g = null
            }
        };
        var f = new b;
        e.prototype.D = function(a) {
            var b = this.o();
            a.Fb(b.resolve, b.reject)
        };
        e.prototype.F = function(a, b) {
            var c = this.o();
            try {
                a.call(b, c.resolve, c.reject)
            } catch (l) {
                c.reject(l)
            }
        };
        e.prototype.then = function(a, b) {
            function c(a, b) {
                return "function" == typeof a ? function(b) {
                    try {
                        d(a(b))
                    } catch (V) {
                        f(V)
                    }
                } : b
            }
            var d, f, g = new e(function(a, b) {
                d = a;
                f = b
            });
            this.Fb(c(a, d), c(b, f));
            return g
        };
        e.prototype["catch"] = function(a) {
            return this.then(void 0, a)
        };
        e.prototype.Fb = function(a, b) {
            function c() {
                switch (d.h) {
                    case 1:
                        a(d.l);
                        break;
                    case 2:
                        b(d.l);
                        break;
                    default:
                        throw Error("Unexpected state: " + d.h);
                }
            }
            var d = this;
            null == this.g ? f.h(c) : this.g.push(c)
        };
        e.resolve = c;
        e.reject = function(a) {
            return new e(function(b, c) {
                c(a)
            })
        };
        e.race = function(a) {
            return new e(function(b, d) {
                for (var e = ra(a), f = e.next(); !f.done; f = e.next()) c(f.value).Fb(b, d)
            })
        };
        e.all = function(a) {
            var b = ra(a),
                d = b.next();
            return d.done ? c([]) : new e(function(a, e) {
                function f(b) {
                    return function(c) {
                        g[b] = c;
                        k--;
                        0 == k && a(g)
                    }
                }
                var g = [],
                    k = 0;
                do g.push(void 0), k++, c(d.value).Fb(f(g.length - 1), e), d =
                    b.next(); while (!d.done)
            })
        };
        return e
    });
    var n = this,
        p = function(a) {
            return void 0 !== a
        },
        q = function(a) {
            return "string" == typeof a
        },
        ya = function(a) {
            return "boolean" == typeof a
        },
        r = function(a) {
            return "number" == typeof a
        },
        u = function(a, b, c) {
            a = a.split(".");
            c = c || n;
            a[0] in c || "undefined" == typeof c.execScript || c.execScript("var " + a[0]);
            for (var d; a.length && (d = a.shift());) !a.length && p(b) ? c[d] = b : c[d] && c[d] !== Object.prototype[d] ? c = c[d] : c = c[d] = {}
        },
        Ba = function() {
            if (null === za) {
                a: {
                    var a = n.document;
                    if ((a = a.querySelector && a.querySelector("script[nonce]")) && (a = a.nonce ||
                            a.getAttribute("nonce")) && Aa.test(a)) break a;a = null
                }
                za = a || ""
            }
            return za
        },
        Aa = /^[\w+/_-]+[=]{0,2}$/,
        za = null,
        Ca = function(a, b) {
            a = a.split(".");
            b = b || n;
            for (var c = 0; c < a.length; c++)
                if (b = b[a[c]], null == b) return null;
            return b
        },
        Da = function() {},
        Ea = function(a) {
            a.jc = void 0;
            a.C = function() {
                return a.jc ? a.jc : a.jc = new a
            }
        },
        Fa = function(a) {
            var b = typeof a;
            if ("object" == b)
                if (a) {
                    if (a instanceof Array) return "array";
                    if (a instanceof Object) return b;
                    var c = Object.prototype.toString.call(a);
                    if ("[object Window]" == c) return "object";
                    if ("[object Array]" ==
                        c || "number" == typeof a.length && "undefined" != typeof a.splice && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("splice")) return "array";
                    if ("[object Function]" == c || "undefined" != typeof a.call && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("call")) return "function"
                } else return "null";
            else if ("function" == b && "undefined" == typeof a.call) return "object";
            return b
        },
        Ga = function(a) {
            return "array" == Fa(a)
        },
        Ha = function(a) {
            var b = Fa(a);
            return "array" == b || "object" == b && "number" == typeof a.length
        },
        v = function(a) {
            return "function" == Fa(a)
        },
        Ia = function(a) {
            var b = typeof a;
            return "object" == b && null != a || "function" == b
        },
        Ja = "closure_uid_" + (1E9 * Math.random() >>> 0),
        Ka = 0,
        Ma = function(a, b, c) {
            return a.call.apply(a.bind, arguments)
        },
        Na = function(a, b, c) {
            if (!a) throw Error();
            if (2 < arguments.length) {
                var d = Array.prototype.slice.call(arguments, 2);
                return function() {
                    var c = Array.prototype.slice.call(arguments);
                    Array.prototype.unshift.apply(c, d);
                    return a.apply(b, c)
                }
            }
            return function() {
                return a.apply(b, arguments)
            }
        },
        w = function(a,
            b, c) {
            Function.prototype.bind && -1 != Function.prototype.bind.toString().indexOf("native code") ? w = Ma : w = Na;
            return w.apply(null, arguments)
        },
        Oa = function(a, b) {
            var c = Array.prototype.slice.call(arguments, 1);
            return function() {
                var b = c.slice();
                b.push.apply(b, arguments);
                return a.apply(this, b)
            }
        },
        x = Date.now || function() {
            return +new Date
        },
        y = function(a, b) {
            function c() {}
            c.prototype = b.prototype;
            a.da = b.prototype;
            a.prototype = new c;
            a.prototype.constructor = a;
            a.xi = function(a, c, f) {
                for (var d = Array(arguments.length - 2), e = 2; e < arguments.length; e++) d[e -
                    2] = arguments[e];
                return b.prototype[c].apply(a, d)
            }
        };
    var Pa = function(a, b, c) {
            for (var d in a) b.call(c, a[d], d, a)
        },
        Qa = function(a, b) {
            var c = {},
                d;
            for (d in a) b.call(void 0, a[d], d, a) && (c[d] = a[d]);
            return c
        },
        Sa = function(a) {
            var b = Ra,
                c;
            for (c in b)
                if (a.call(void 0, b[c], c, b)) return !0;
            return !1
        },
        Ua = function(a) {
            var b = [],
                c = 0,
                d;
            for (d in a) b[c++] = a[d];
            return b
        },
        Va = function(a) {
            var b = [],
                c = 0,
                d;
            for (d in a) b[c++] = d;
            return b
        },
        Wa = function(a, b) {
            var c = Ha(b),
                d = c ? b : arguments;
            for (c = c ? 0 : 1; c < d.length; c++) {
                if (null == a) return;
                a = a[d[c]]
            }
            return a
        },
        Xa = function(a, b) {
            return null !== a && b in a
        },
        Ya = function(a, b) {
            for (var c in a)
                if (a[c] == b) return !0;
            return !1
        },
        $a = function(a) {
            var b = Za,
                c;
            for (c in b)
                if (a.call(void 0, b[c], c, b)) return c
        },
        ab = function(a) {
            for (var b in a) return !1;
            return !0
        },
        bb = function(a, b, c) {
            return null !== a && b in a ? a[b] : c
        },
        cb = function(a) {
            var b = {},
                c;
            for (c in a) b[c] = a[c];
            return b
        },
        db = "constructor hasOwnProperty isPrototypeOf propertyIsEnumerable toLocaleString toString valueOf".split(" "),
        eb = function(a, b) {
            for (var c, d, e = 1; e < arguments.length; e++) {
                d = arguments[e];
                for (c in d) a[c] = d[c];
                for (var f =
                        0; f < db.length; f++) c = db[f], Object.prototype.hasOwnProperty.call(d, c) && (a[c] = d[c])
            }
        };
    n.console && "function" === typeof n.console.log && w(n.console.log, n.console);
    var fb;
    var gb = function(a, b) {
            if (q(a)) return q(b) && 1 == b.length ? a.indexOf(b, 0) : -1;
            for (var c = 0; c < a.length; c++)
                if (c in a && a[c] === b) return c;
            return -1
        },
        z = function(a, b, c) {
            for (var d = a.length, e = q(a) ? a.split("") : a, f = 0; f < d; f++) f in e && b.call(c, e[f], f, a)
        },
        ib = function(a, b) {
            for (var c = q(a) ? a.split("") : a, d = a.length - 1; 0 <= d; --d) d in c && b.call(void 0, c[d], d, a)
        },
        jb = function(a, b) {
            for (var c = a.length, d = [], e = 0, f = q(a) ? a.split("") : a, g = 0; g < c; g++)
                if (g in f) {
                    var k = f[g];
                    b.call(void 0, k, g, a) && (d[e++] = k)
                }
            return d
        },
        kb = function(a, b) {
            for (var c =
                    a.length, d = Array(c), e = q(a) ? a.split("") : a, f = 0; f < c; f++) f in e && (d[f] = b.call(void 0, e[f], f, a));
            return d
        },
        lb = function(a, b, c) {
            var d = c;
            z(a, function(c, f) {
                d = b.call(void 0, d, c, f, a)
            });
            return d
        },
        nb = function(a, b) {
            for (var c = a.length, d = q(a) ? a.split("") : a, e = 0; e < c; e++)
                if (e in d && b.call(void 0, d[e], e, a)) return !0;
            return !1
        },
        pb = function(a, b) {
            b = ob(a, b, void 0);
            return 0 > b ? null : q(a) ? a.charAt(b) : a[b]
        },
        ob = function(a, b, c) {
            for (var d = a.length, e = q(a) ? a.split("") : a, f = 0; f < d; f++)
                if (f in e && b.call(c, e[f], f, a)) return f;
            return -1
        },
        qb =
        function(a, b) {
            for (var c = q(a) ? a.split("") : a, d = a.length - 1; 0 <= d; d--)
                if (d in c && b.call(void 0, c[d], d, a)) return d;
            return -1
        },
        rb = function(a, b) {
            return 0 <= gb(a, b)
        },
        tb = function(a, b) {
            b = gb(a, b);
            var c;
            (c = 0 <= b) && sb(a, b);
            return c
        },
        sb = function(a, b) {
            return 1 == Array.prototype.splice.call(a, b, 1).length
        },
        ub = function(a, b) {
            var c = 0;
            ib(a, function(d, e) {
                b.call(void 0, d, e, a) && sb(a, e) && c++
            })
        },
        wb = function(a) {
            return Array.prototype.concat.apply([], arguments)
        },
        xb = function(a) {
            var b = a.length;
            if (0 < b) {
                for (var c = Array(b), d = 0; d < b; d++) c[d] =
                    a[d];
                return c
            }
            return []
        },
        yb = function(a) {
            for (var b = {}, c = 0, d = 0; d < a.length;) {
                var e = a[d++];
                var f = e;
                f = Ia(f) ? "o" + (f[Ja] || (f[Ja] = ++Ka)) : (typeof f).charAt(0) + f;
                Object.prototype.hasOwnProperty.call(b, f) || (b[f] = !0, a[c++] = e)
            }
            a.length = c
        },
        Ab = function(a, b) {
            a.sort(b || zb)
        },
        zb = function(a, b) {
            return a > b ? 1 : a < b ? -1 : 0
        },
        Bb = function(a) {
            for (var b = [], c = 0; c < a; c++) b[c] = "";
            return b
        };
    var Cb = function(a, b) {
            var c = a.length - b.length;
            return 0 <= c && a.indexOf(b, c) == c
        },
        A = function(a) {
            return /^[\s\xa0]*$/.test(a)
        },
        Db = String.prototype.trim ? function(a) {
            return a.trim()
        } : function(a) {
            return /^[\s\xa0]*([\s\S]*?)[\s\xa0]*$/.exec(a)[1]
        },
        Eb = function(a) {
            return decodeURIComponent(a.replace(/\+/g, " "))
        },
        Mb = function(a) {
            if (!Fb.test(a)) return a; - 1 != a.indexOf("&") && (a = a.replace(Gb, "&amp;")); - 1 != a.indexOf("<") && (a = a.replace(Hb, "&lt;")); - 1 != a.indexOf(">") && (a = a.replace(Ib, "&gt;")); - 1 != a.indexOf('"') && (a = a.replace(Jb,
                "&quot;")); - 1 != a.indexOf("'") && (a = a.replace(Kb, "&#39;")); - 1 != a.indexOf("\x00") && (a = a.replace(Lb, "&#0;"));
            return a
        },
        Gb = /&/g,
        Hb = /</g,
        Ib = />/g,
        Jb = /"/g,
        Kb = /'/g,
        Lb = /\x00/g,
        Fb = /[\x00&<>"']/,
        Nb = function(a, b) {
            a.length > b && (a = a.substring(0, b - 3) + "...");
            return a
        },
        B = function(a, b) {
            return -1 != a.toLowerCase().indexOf(b.toLowerCase())
        },
        Ob = String.prototype.repeat ? function(a, b) {
            return a.repeat(b)
        } : function(a, b) {
            return Array(b + 1).join(a)
        },
        Pb = function(a, b) {
            a = p(void 0) ? a.toFixed(void 0) : String(a);
            var c = a.indexOf("."); - 1 ==
                c && (c = a.length);
            Ob("0", Math.max(0, b - c))
        },
        Qb = function(a) {
            return null == a ? "" : String(a)
        },
        Sb = function(a, b) {
            var c = 0;
            a = Db(String(a)).split(".");
            b = Db(String(b)).split(".");
            for (var d = Math.max(a.length, b.length), e = 0; 0 == c && e < d; e++) {
                var f = a[e] || "",
                    g = b[e] || "";
                do {
                    f = /(\d*)(\D*)(.*)/.exec(f) || ["", "", "", ""];
                    g = /(\d*)(\D*)(.*)/.exec(g) || ["", "", "", ""];
                    if (0 == f[0].length && 0 == g[0].length) break;
                    c = Rb(0 == f[1].length ? 0 : parseInt(f[1], 10), 0 == g[1].length ? 0 : parseInt(g[1], 10)) || Rb(0 == f[2].length, 0 == g[2].length) || Rb(f[2], g[2]);
                    f =
                        f[3];
                    g = g[3]
                } while (0 == c)
            }
            return c
        },
        Rb = function(a, b) {
            return a < b ? -1 : a > b ? 1 : 0
        },
        Tb = 2147483648 * Math.random() | 0,
        Ub = function(a) {
            var b = Number(a);
            return 0 == b && A(a) ? NaN : b
        },
        Vb = function(a) {
            return String(a).replace(/\-([a-z])/g, function(a, c) {
                return c.toUpperCase()
            })
        },
        Wb = function() {
            return "googleAvInapp".replace(/([A-Z])/g, "-$1").toLowerCase()
        },
        Xb = function(a) {
            var b = q(void 0) ? "undefined".replace(/([-()\[\]{}+?*.$\^|,:#<!\\])/g, "\\$1").replace(/\x08/g, "\\x08") : "\\s";
            return a.replace(new RegExp("(^" + (b ? "|[" + b + "]+" : "") +
                ")([a-z])", "g"), function(a, b, e) {
                return b + e.toUpperCase()
            })
        };
    var C;
    a: {
        var Zb = n.navigator;
        if (Zb) {
            var $b = Zb.userAgent;
            if ($b) {
                C = $b;
                break a
            }
        }
        C = ""
    }
    var D = function(a) {
            return -1 != C.indexOf(a)
        },
        ac = function(a) {
            for (var b = /(\w[\w ]+)\/([^\s]+)\s*(?:\((.*?)\))?/g, c = [], d; d = b.exec(a);) c.push([d[1], d[2], d[3] || void 0]);
            return c
        };
    var cc = function() {
            return D("Trident") || D("MSIE")
        },
        gc = function() {
            return D("Safari") && !(fc() || D("Coast") || D("Opera") || D("Edge") || D("Silk") || D("Android"))
        },
        fc = function() {
            return (D("Chrome") || D("CriOS")) && !D("Edge")
        },
        ic = function() {
            function a(a) {
                a = pb(a, d);
                return c[a] || ""
            }
            var b = C;
            if (cc()) return hc(b);
            b = ac(b);
            var c = {};
            z(b, function(a) {
                c[a[0]] = a[1]
            });
            var d = Oa(Xa, c);
            return D("Opera") ? a(["Version", "Opera"]) : D("Edge") ? a(["Edge"]) : fc() ? a(["Chrome", "CriOS"]) : (b = b[2]) && b[1] || ""
        },
        hc = function(a) {
            var b = /rv: *([\d\.]*)/.exec(a);
            if (b && b[1]) return b[1];
            b = "";
            var c = /MSIE +([\d\.]+)/.exec(a);
            if (c && c[1])
                if (a = /Trident\/(\d.\d)/.exec(a), "7.0" == c[1])
                    if (a && a[1]) switch (a[1]) {
                        case "4.0":
                            b = "8.0";
                            break;
                        case "5.0":
                            b = "9.0";
                            break;
                        case "6.0":
                            b = "10.0";
                            break;
                        case "7.0":
                            b = "11.0"
                    } else b = "7.0";
                    else b = c[1];
            return b
        };
    var jc = function() {
        return D("iPhone") && !D("iPod") && !D("iPad")
    };
    var kc = function(a) {
        kc[" "](a);
        return a
    };
    kc[" "] = Da;
    var lc = function(a, b) {
            try {
                return kc(a[b]), !0
            } catch (c) {}
            return !1
        },
        nc = function(a, b) {
            var c = mc;
            return Object.prototype.hasOwnProperty.call(c, a) ? c[a] : c[a] = b(a)
        };
    var oc = D("Opera"),
        pc = cc(),
        qc = D("Edge"),
        rc = D("Gecko") && !(B(C, "WebKit") && !D("Edge")) && !(D("Trident") || D("MSIE")) && !D("Edge"),
        sc = B(C, "WebKit") && !D("Edge"),
        tc = D("Android"),
        uc = jc(),
        vc = D("iPad"),
        wc = function() {
            var a = n.document;
            return a ? a.documentMode : void 0
        },
        yc;
    a: {
        var zc = "",
            Ac = function() {
                var a = C;
                if (rc) return /rv:([^\);]+)(\)|;)/.exec(a);
                if (qc) return /Edge\/([\d\.]+)/.exec(a);
                if (pc) return /\b(?:MSIE|rv)[: ]([^\);]+)(\)|;)/.exec(a);
                if (sc) return /WebKit\/(\S+)/.exec(a);
                if (oc) return /(?:Version)[ \/]?(\S+)/.exec(a)
            }();Ac && (zc = Ac ? Ac[1] : "");
        if (pc) {
            var Bc = wc();
            if (null != Bc && Bc > parseFloat(zc)) {
                yc = String(Bc);
                break a
            }
        }
        yc = zc
    }
    var Cc = yc,
        mc = {},
        Dc = function(a) {
            return nc(a, function() {
                return 0 <= Sb(Cc, a)
            })
        },
        Ec;
    var Fc = n.document;
    Ec = Fc && pc ? wc() || ("CSS1Compat" == Fc.compatMode ? parseInt(Cc, 10) : 5) : void 0;
    var Gc = !pc || 9 <= Number(Ec),
        Hc = pc || oc || sc;
    var Jc = function() {
        this.g = "";
        this.h = Ic
    };
    Jc.prototype.Va = !0;
    Jc.prototype.Ha = function() {
        return this.g
    };
    Jc.prototype.toString = function() {
        return "Const{" + this.g + "}"
    };
    var Kc = function(a) {
            return a instanceof Jc && a.constructor === Jc && a.h === Ic ? a.g : "type_error:Const"
        },
        Ic = {},
        Lc = function(a) {
            var b = new Jc;
            b.g = a;
            return b
        };
    Lc("");
    var Nc = function() {
        this.g = "";
        this.h = Mc
    };
    Nc.prototype.Va = !0;
    Nc.prototype.Ha = function() {
        return this.g
    };
    Nc.prototype.hc = !0;
    Nc.prototype.Zb = function() {
        return 1
    };
    var Oc = function(a) {
            if (a instanceof Nc && a.constructor === Nc && a.h === Mc) return a.g;
            Fa(a);
            return "type_error:TrustedResourceUrl"
        },
        Mc = {},
        Pc = function(a) {
            var b = new Nc;
            b.g = a;
            return b
        };
    var Rc = function() {
        this.g = "";
        this.h = Qc
    };
    Rc.prototype.Va = !0;
    Rc.prototype.Ha = function() {
        return this.g
    };
    Rc.prototype.hc = !0;
    Rc.prototype.Zb = function() {
        return 1
    };
    var Sc = function(a) {
            if (a instanceof Rc && a.constructor === Rc && a.h === Qc) return a.g;
            Fa(a);
            return "type_error:SafeUrl"
        },
        Tc = /^(?:(?:https?|mailto|ftp):|[^:/?#]*(?:[/?#]|$))/i,
        Qc = {},
        Uc = function(a) {
            var b = new Rc;
            b.g = a;
            return b
        };
    Uc("about:blank");
    var Wc = function() {
        this.g = "";
        this.l = Vc;
        this.h = null
    };
    Wc.prototype.hc = !0;
    Wc.prototype.Zb = function() {
        return this.h
    };
    Wc.prototype.Va = !0;
    Wc.prototype.Ha = function() {
        return this.g
    };
    var Vc = {},
        Xc = function(a, b) {
            var c = new Wc;
            c.g = a;
            c.h = b;
            return c
        };
    Xc("<!DOCTYPE html>", 0);
    Xc("", 0);
    Xc("<br>", 0);
    var Yc = function(a, b) {
        a.src = Oc(b);
        (b = Ba()) && a.setAttribute("nonce", b)
    };
    var Zc = function(a, b) {
        this.x = p(a) ? a : 0;
        this.y = p(b) ? b : 0
    };
    h = Zc.prototype;
    h.clone = function() {
        return new Zc(this.x, this.y)
    };
    h.ceil = function() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        return this
    };
    h.floor = function() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        return this
    };
    h.round = function() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        return this
    };
    h.scale = function(a, b) {
        b = r(b) ? b : a;
        this.x *= a;
        this.y *= b;
        return this
    };
    var E = function(a, b) {
            this.width = a;
            this.height = b
        },
        $c = function(a, b) {
            return a == b ? !0 : a && b ? a.width == b.width && a.height == b.height : !1
        };
    h = E.prototype;
    h.clone = function() {
        return new E(this.width, this.height)
    };
    h.Fa = function() {
        return this.width * this.height
    };
    h.aspectRatio = function() {
        return this.width / this.height
    };
    h.isEmpty = function() {
        return !this.Fa()
    };
    h.ceil = function() {
        this.width = Math.ceil(this.width);
        this.height = Math.ceil(this.height);
        return this
    };
    h.floor = function() {
        this.width = Math.floor(this.width);
        this.height = Math.floor(this.height);
        return this
    };
    h.round = function() {
        this.width = Math.round(this.width);
        this.height = Math.round(this.height);
        return this
    };
    h.scale = function(a, b) {
        b = r(b) ? b : a;
        this.width *= a;
        this.height *= b;
        return this
    };
    var cd = function(a) {
            return a ? new ad(bd(a)) : fb || (fb = new ad)
        },
        dd = function() {
            var a = document;
            return a.querySelectorAll && a.querySelector ? a.querySelectorAll("SCRIPT") : a.getElementsByTagName("SCRIPT")
        },
        hd = function(a, b) {
            Pa(b, function(b, d) {
                b && b.Va && (b = b.Ha());
                "style" == d ? a.style.cssText = b : "class" == d ? a.className = b : "for" == d ? a.htmlFor = b : ed.hasOwnProperty(d) ? a.setAttribute(ed[d], b) : 0 == d.lastIndexOf("aria-", 0) || 0 == d.lastIndexOf("data-", 0) ? a.setAttribute(d, b) : a[d] = b
            })
        },
        ed = {
            cellpadding: "cellPadding",
            cellspacing: "cellSpacing",
            colspan: "colSpan",
            frameborder: "frameBorder",
            height: "height",
            maxlength: "maxLength",
            nonce: "nonce",
            role: "role",
            rowspan: "rowSpan",
            type: "type",
            usemap: "useMap",
            valign: "vAlign",
            width: "width"
        },
        id = function(a) {
            a = a.document;
            a = "CSS1Compat" == a.compatMode ? a.documentElement : a.body;
            return new E(a.clientWidth, a.clientHeight)
        },
        jd = function(a) {
            var b = a.scrollingElement ? a.scrollingElement : sc || "CSS1Compat" != a.compatMode ? a.body || a.documentElement : a.documentElement;
            a = a.parentWindow || a.defaultView;
            return pc && Dc("10") && a.pageYOffset !=
                b.scrollTop ? new Zc(b.scrollLeft, b.scrollTop) : new Zc(a.pageXOffset || b.scrollLeft, a.pageYOffset || b.scrollTop)
        },
        F = function(a) {
            return a ? a.parentWindow || a.defaultView : window
        },
        ld = function(a, b, c) {
            var d = arguments,
                e = document,
                f = String(d[0]),
                g = d[1];
            if (!Gc && g && (g.name || g.type)) {
                f = ["<", f];
                g.name && f.push(' name="', Mb(g.name), '"');
                if (g.type) {
                    f.push(' type="', Mb(g.type), '"');
                    var k = {};
                    eb(k, g);
                    delete k.type;
                    g = k
                }
                f.push(">");
                f = f.join("")
            }
            f = e.createElement(f);
            g && (q(g) ? f.className = g : Ga(g) ? f.className = g.join(" ") : hd(f,
                g));
            2 < d.length && kd(e, f, d);
            return f
        },
        kd = function(a, b, c) {
            function d(c) {
                c && b.appendChild(q(c) ? a.createTextNode(c) : c)
            }
            for (var e = 2; e < c.length; e++) {
                var f = c[e];
                !Ha(f) || Ia(f) && 0 < f.nodeType ? d(f) : z(md(f) ? xb(f) : f, d)
            }
        },
        nd = function(a) {
            a && a.parentNode && a.parentNode.removeChild(a)
        },
        od = function(a) {
            var b;
            if (Hc && !(pc && Dc("9") && !Dc("10") && n.SVGElement && a instanceof n.SVGElement) && (b = a.parentElement)) return b;
            b = a.parentNode;
            return Ia(b) && 1 == b.nodeType ? b : null
        },
        pd = function(a, b) {
            if (!a || !b) return !1;
            if (a.contains && 1 == b.nodeType) return a ==
                b || a.contains(b);
            if ("undefined" != typeof a.compareDocumentPosition) return a == b || !!(a.compareDocumentPosition(b) & 16);
            for (; b && a != b;) b = b.parentNode;
            return b == a
        },
        bd = function(a) {
            return 9 == a.nodeType ? a : a.ownerDocument || a.document
        },
        qd = function(a) {
            try {
                return a.contentWindow || (a.contentDocument ? F(a.contentDocument) : null)
            } catch (b) {}
            return null
        },
        md = function(a) {
            if (a && "number" == typeof a.length) {
                if (Ia(a)) return "function" == typeof a.item || "string" == typeof a.item;
                if (v(a)) return "function" == typeof a.item
            }
            return !1
        },
        rd =
        function(a, b) {
            a && (a = a.parentNode);
            for (var c = 0; a;) {
                if (b(a)) return a;
                a = a.parentNode;
                c++
            }
            return null
        },
        ad = function(a) {
            this.g = a || n.document || document
        };
    ad.prototype.createElement = function(a) {
        return this.g.createElement(String(a))
    };
    ad.prototype.contains = pd;
    var sd = function(a) {
        for (var b = [], c = a = F(a.ownerDocument); c != a.top; c = c.parent)
            if (c.frameElement) b.push(c.frameElement);
            else break;
        return b
    };
    var td = !pc || 9 <= Number(Ec),
        ud = pc && !Dc("9"),
        vd = function() {
            if (!n.addEventListener || !Object.defineProperty) return !1;
            var a = !1,
                b = Object.defineProperty({}, "passive", {
                    get: function() {
                        a = !0
                    }
                });
            n.addEventListener("test", Da, b);
            n.removeEventListener("test", Da, b);
            return a
        }();
    var wd = function() {
        this.J = this.J;
        this.H = this.H
    };
    wd.prototype.J = !1;
    wd.prototype.Mb = function() {
        return this.J
    };
    wd.prototype.W = function() {
        this.J || (this.J = !0, this.T())
    };
    var xd = function(a, b) {
        a.J ? p(void 0) ? b.call(void 0) : b() : (a.H || (a.H = []), a.H.push(p(void 0) ? w(b, void 0) : b))
    };
    wd.prototype.T = function() {
        if (this.H)
            for (; this.H.length;) this.H.shift()()
    };
    var yd = function(a) {
        a && "function" == typeof a.W && a.W()
    };
    var zd = function(a, b) {
        this.type = a;
        this.g = this.target = b;
        this.od = !0
    };
    zd.prototype.l = function() {
        this.od = !1
    };
    var Bd = function(a, b) {
        zd.call(this, a ? a.type : "");
        this.relatedTarget = this.g = this.target = null;
        this.button = this.screenY = this.screenX = this.clientY = this.clientX = 0;
        this.key = "";
        this.metaKey = this.shiftKey = this.altKey = this.ctrlKey = !1;
        this.pointerId = 0;
        this.pointerType = "";
        this.h = null;
        if (a) {
            var c = this.type = a.type,
                d = a.changedTouches ? a.changedTouches[0] : null;
            this.target = a.target || a.srcElement;
            this.g = b;
            (b = a.relatedTarget) ? rc && (lc(b, "nodeName") || (b = null)): "mouseover" == c ? b = a.fromElement : "mouseout" == c && (b = a.toElement);
            this.relatedTarget = b;
            null === d ? (this.clientX = void 0 !== a.clientX ? a.clientX : a.pageX, this.clientY = void 0 !== a.clientY ? a.clientY : a.pageY, this.screenX = a.screenX || 0, this.screenY = a.screenY || 0) : (this.clientX = void 0 !== d.clientX ? d.clientX : d.pageX, this.clientY = void 0 !== d.clientY ? d.clientY : d.pageY, this.screenX = d.screenX || 0, this.screenY = d.screenY || 0);
            this.button = a.button;
            this.key = a.key || "";
            this.ctrlKey = a.ctrlKey;
            this.altKey = a.altKey;
            this.shiftKey = a.shiftKey;
            this.metaKey = a.metaKey;
            this.pointerId = a.pointerId || 0;
            this.pointerType =
                q(a.pointerType) ? a.pointerType : Ad[a.pointerType] || "";
            this.h = a;
            a.defaultPrevented && this.l()
        }
    };
    y(Bd, zd);
    var Ad = {
        2: "touch",
        3: "pen",
        4: "mouse"
    };
    Bd.prototype.l = function() {
        Bd.da.l.call(this);
        var a = this.h;
        if (a.preventDefault) a.preventDefault();
        else if (a.returnValue = !1, ud) try {
            if (a.ctrlKey || 112 <= a.keyCode && 123 >= a.keyCode) a.keyCode = -1
        } catch (b) {}
    };
    var Cd = "closure_listenable_" + (1E6 * Math.random() | 0),
        Dd = function(a) {
            return !(!a || !a[Cd])
        },
        Ed = 0;
    var Fd = function(a, b, c, d, e) {
            this.listener = a;
            this.g = null;
            this.src = b;
            this.type = c;
            this.capture = !!d;
            this.Ib = e;
            this.key = ++Ed;
            this.kb = this.Eb = !1
        },
        Gd = function(a) {
            a.kb = !0;
            a.listener = null;
            a.g = null;
            a.src = null;
            a.Ib = null
        };
    var Hd = function(a) {
        this.src = a;
        this.g = {};
        this.h = 0
    };
    Hd.prototype.add = function(a, b, c, d, e) {
        var f = a.toString();
        a = this.g[f];
        a || (a = this.g[f] = [], this.h++);
        var g = Id(a, b, d, e); - 1 < g ? (b = a[g], c || (b.Eb = !1)) : (b = new Fd(b, this.src, f, !!d, e), b.Eb = c, a.push(b));
        return b
    };
    var Jd = function(a, b) {
            var c = b.type;
            c in a.g && tb(a.g[c], b) && (Gd(b), 0 == a.g[c].length && (delete a.g[c], a.h--))
        },
        Kd = function(a, b, c, d, e) {
            a = a.g[b.toString()];
            b = -1;
            a && (b = Id(a, c, d, e));
            return -1 < b ? a[b] : null
        },
        Id = function(a, b, c, d) {
            for (var e = 0; e < a.length; ++e) {
                var f = a[e];
                if (!f.kb && f.listener == b && f.capture == !!c && f.Ib == d) return e
            }
            return -1
        };
    var Ld = "closure_lm_" + (1E6 * Math.random() | 0),
        Md = {},
        Nd = 0,
        Pd = function(a, b, c, d, e) {
            if (d && d.once) return Od(a, b, c, d, e);
            if (Ga(b)) {
                for (var f = 0; f < b.length; f++) Pd(a, b[f], c, d, e);
                return null
            }
            c = Qd(c);
            return Dd(a) ? a.N(b, c, Ia(d) ? !!d.capture : !!d, e) : Rd(a, b, c, !1, d, e)
        },
        Rd = function(a, b, c, d, e, f) {
            if (!b) throw Error("Invalid event type");
            var g = Ia(e) ? !!e.capture : !!e,
                k = Td(a);
            k || (a[Ld] = k = new Hd(a));
            c = k.add(b, c, d, g, f);
            if (c.g) return c;
            d = Ud();
            c.g = d;
            d.src = a;
            d.listener = c;
            if (a.addEventListener) vd || (e = g), void 0 === e && (e = !1), a.addEventListener(b.toString(),
                d, e);
            else if (a.attachEvent) a.attachEvent(Vd(b.toString()), d);
            else if (a.addListener && a.removeListener) a.addListener(d);
            else throw Error("addEventListener and attachEvent are unavailable.");
            Nd++;
            return c
        },
        Ud = function() {
            var a = Wd,
                b = td ? function(c) {
                    return a.call(b.src, b.listener, c)
                } : function(c) {
                    c = a.call(b.src, b.listener, c);
                    if (!c) return c
                };
            return b
        },
        Od = function(a, b, c, d, e) {
            if (Ga(b)) {
                for (var f = 0; f < b.length; f++) Od(a, b[f], c, d, e);
                return null
            }
            c = Qd(c);
            return Dd(a) ? a.A.add(String(b), c, !0, Ia(d) ? !!d.capture : !!d, e) :
                Rd(a, b, c, !0, d, e)
        },
        Xd = function(a, b, c, d, e) {
            if (Ga(b))
                for (var f = 0; f < b.length; f++) Xd(a, b[f], c, d, e);
            else d = Ia(d) ? !!d.capture : !!d, c = Qd(c), Dd(a) ? a.Na(b, c, d, e) : a && (a = Td(a)) && (b = Kd(a, b, c, d, e)) && Yd(b)
        },
        Yd = function(a) {
            if (!r(a) && a && !a.kb) {
                var b = a.src;
                if (Dd(b)) Jd(b.A, a);
                else {
                    var c = a.type,
                        d = a.g;
                    b.removeEventListener ? b.removeEventListener(c, d, a.capture) : b.detachEvent ? b.detachEvent(Vd(c), d) : b.addListener && b.removeListener && b.removeListener(d);
                    Nd--;
                    (c = Td(b)) ? (Jd(c, a), 0 == c.h && (c.src = null, b[Ld] = null)) : Gd(a)
                }
            }
        },
        Vd = function(a) {
            return a in
                Md ? Md[a] : Md[a] = "on" + a
        },
        $d = function(a, b, c, d) {
            var e = !0;
            if (a = Td(a))
                if (b = a.g[b.toString()])
                    for (b = b.concat(), a = 0; a < b.length; a++) {
                        var f = b[a];
                        f && f.capture == c && !f.kb && (f = Zd(f, d), e = e && !1 !== f)
                    }
            return e
        },
        Zd = function(a, b) {
            var c = a.listener,
                d = a.Ib || a.src;
            a.Eb && Yd(a);
            return c.call(d, b)
        },
        Wd = function(a, b) {
            if (a.kb) return !0;
            if (!td) {
                var c = b || Ca("window.event");
                b = new Bd(c, this);
                var d = !0;
                if (!(0 > c.keyCode || void 0 != c.returnValue)) {
                    a: {
                        var e = !1;
                        if (0 == c.keyCode) try {
                            c.keyCode = -1;
                            break a
                        } catch (g) {
                            e = !0
                        }
                        if (e || void 0 == c.returnValue) c.returnValue = !0
                    }
                    c = [];
                    for (e = b.g; e; e = e.parentNode) c.push(e);a = a.type;
                    for (e = c.length - 1; 0 <= e; e--) {
                        b.g = c[e];
                        var f = $d(c[e], a, !0, b);
                        d = d && f
                    }
                    for (e = 0; e < c.length; e++) b.g = c[e],
                    f = $d(c[e], a, !1, b),
                    d = d && f
                }
                return d
            }
            return Zd(a, new Bd(b, this))
        },
        Td = function(a) {
            a = a[Ld];
            return a instanceof Hd ? a : null
        },
        ae = "__closure_events_fn_" + (1E9 * Math.random() >>> 0),
        Qd = function(a) {
            if (v(a)) return a;
            a[ae] || (a[ae] = function(b) {
                return a.handleEvent(b)
            });
            return a[ae]
        };
    var G = function() {
        wd.call(this);
        this.A = new Hd(this);
        this.Ub = this;
        this.Oa = null
    };
    y(G, wd);
    G.prototype[Cd] = !0;
    h = G.prototype;
    h.addEventListener = function(a, b, c, d) {
        Pd(this, a, b, c, d)
    };
    h.removeEventListener = function(a, b, c, d) {
        Xd(this, a, b, c, d)
    };
    h.dispatchEvent = function(a) {
        var b, c = this.Oa;
        if (c)
            for (b = []; c; c = c.Oa) b.push(c);
        c = this.Ub;
        var d = a.type || a;
        if (q(a)) a = new zd(a, c);
        else if (a instanceof zd) a.target = a.target || c;
        else {
            var e = a;
            a = new zd(d, c);
            eb(a, e)
        }
        e = !0;
        if (b)
            for (var f = b.length - 1; 0 <= f; f--) {
                var g = a.g = b[f];
                e = be(g, d, !0, a) && e
            }
        g = a.g = c;
        e = be(g, d, !0, a) && e;
        e = be(g, d, !1, a) && e;
        if (b)
            for (f = 0; f < b.length; f++) g = a.g = b[f], e = be(g, d, !1, a) && e;
        return e
    };
    h.T = function() {
        G.da.T.call(this);
        if (this.A) {
            var a = this.A,
                b = 0,
                c;
            for (c in a.g) {
                for (var d = a.g[c], e = 0; e < d.length; e++) ++b, Gd(d[e]);
                delete a.g[c];
                a.h--
            }
        }
        this.Oa = null
    };
    h.N = function(a, b, c, d) {
        return this.A.add(String(a), b, !1, c, d)
    };
    h.Na = function(a, b, c, d) {
        var e = this.A;
        a = String(a).toString();
        if (a in e.g) {
            var f = e.g[a];
            b = Id(f, b, c, d); - 1 < b && (Gd(f[b]), sb(f, b), 0 == f.length && (delete e.g[a], e.h--))
        }
    };
    var be = function(a, b, c, d) {
        b = a.A.g[String(b)];
        if (!b) return !0;
        b = b.concat();
        for (var e = !0, f = 0; f < b.length; ++f) {
            var g = b[f];
            if (g && !g.kb && g.capture == c) {
                var k = g.listener,
                    m = g.Ib || g.src;
                g.Eb && Jd(a.A, g);
                e = !1 !== k.call(m, d) && e
            }
        }
        return e && 0 != d.od
    };
    var ce = function(a) {
            return function() {
                return a
            }
        },
        de = function(a) {
            var b = !1,
                c;
            return function() {
                b || (c = a(), b = !0);
                return c
            }
        };
    var ee = function(a, b) {
        G.call(this);
        this.h = a || 1;
        this.g = b || n;
        this.l = w(this.ag, this);
        this.o = x()
    };
    y(ee, G);
    h = ee.prototype;
    h.vb = !1;
    h.ua = null;
    h.setInterval = function(a) {
        this.h = a;
        this.ua && this.vb ? (this.stop(), this.start()) : this.ua && this.stop()
    };
    h.ag = function() {
        if (this.vb) {
            var a = x() - this.o;
            0 < a && a < .8 * this.h ? this.ua = this.g.setTimeout(this.l, this.h - a) : (this.ua && (this.g.clearTimeout(this.ua), this.ua = null), this.dispatchEvent("tick"), this.vb && (this.stop(), this.start()))
        }
    };
    h.start = function() {
        this.vb = !0;
        this.ua || (this.ua = this.g.setTimeout(this.l, this.h), this.o = x())
    };
    h.stop = function() {
        this.vb = !1;
        this.ua && (this.g.clearTimeout(this.ua), this.ua = null)
    };
    h.T = function() {
        ee.da.T.call(this);
        this.stop();
        delete this.g
    };
    var fe = function(a, b, c) {
        if (v(a)) c && (a = w(a, c));
        else if (a && "function" == typeof a.handleEvent) a = w(a.handleEvent, a);
        else throw Error("Invalid listener argument");
        return 2147483647 < Number(b) ? -1 : n.setTimeout(a, b || 0)
    };
    var ge = function() {
            return Math.round(x() / 1E3)
        },
        he = function(a) {
            var b = window.performance && window.performance.timing && window.performance.timing.domLoading && 0 < window.performance.timing.domLoading ? Math.round(window.performance.timing.domLoading / 1E3) : null;
            return null != b ? b : null != a ? a : ge()
        };
    var ie = function(a) {
        return kb(a, function(a) {
            a = a.toString(16);
            return 1 < a.length ? a : "0" + a
        }).join("")
    };
    var je = D("Firefox"),
        le = jc() || D("iPod"),
        re = D("iPad"),
        se = D("Android") && !(fc() || D("Firefox") || D("Opera") || D("Silk")),
        te = fc(),
        ue = gc() && !(jc() || D("iPad") || D("iPod"));
    var ve = null,
        we = null;
    var xe = function() {
        this.h = -1
    };
    var Ae = function(a) {
            var b = [];
            ye(new ze, a, b);
            return b.join("")
        },
        ze = function() {},
        ye = function(a, b, c) {
            if (null == b) c.push("null");
            else {
                if ("object" == typeof b) {
                    if (Ga(b)) {
                        var d = b;
                        b = d.length;
                        c.push("[");
                        for (var e = "", f = 0; f < b; f++) c.push(e), ye(a, d[f], c), e = ",";
                        c.push("]");
                        return
                    }
                    if (b instanceof String || b instanceof Number || b instanceof Boolean) b = b.valueOf();
                    else {
                        c.push("{");
                        e = "";
                        for (d in b) Object.prototype.hasOwnProperty.call(b, d) && (f = b[d], "function" != typeof f && (c.push(e), Be(d, c), c.push(":"), ye(a, f, c), e = ","));
                        c.push("}");
                        return
                    }
                }
                switch (typeof b) {
                    case "string":
                        Be(b, c);
                        break;
                    case "number":
                        c.push(isFinite(b) && !isNaN(b) ? String(b) : "null");
                        break;
                    case "boolean":
                        c.push(String(b));
                        break;
                    case "function":
                        c.push("null");
                        break;
                    default:
                        throw Error("Unknown type: " + typeof b);
                }
            }
        },
        Ce = {
            '"': '\\"',
            "\\": "\\\\",
            "/": "\\/",
            "\b": "\\b",
            "\f": "\\f",
            "\n": "\\n",
            "\r": "\\r",
            "\t": "\\t",
            "\x0B": "\\u000b"
        },
        De = /\uffff/.test("\uffff") ? /[\\"\x00-\x1f\x7f-\uffff]/g : /[\\"\x00-\x1f\x7f-\xff]/g,
        Be = function(a, b) {
            b.push('"', a.replace(De, function(a) {
                var b = Ce[a];
                b || (b = "\\u" + (a.charCodeAt(0) | 65536).toString(16).substr(1), Ce[a] = b);
                return b
            }), '"')
        };
    var Ee = function(a) {
        this.g = a || {
            cookie: ""
        }
    };
    h = Ee.prototype;
    h.set = function(a, b, c, d, e, f) {
        if (/[;=\s]/.test(a)) throw Error('Invalid cookie name "' + a + '"');
        if (/[;\r\n]/.test(b)) throw Error('Invalid cookie value "' + b + '"');
        p(c) || (c = -1);
        e = e ? ";domain=" + e : "";
        d = d ? ";path=" + d : "";
        f = f ? ";secure" : "";
        c = 0 > c ? "" : 0 == c ? ";expires=" + (new Date(1970, 1, 1)).toUTCString() : ";expires=" + (new Date(x() + 1E3 * c)).toUTCString();
        this.g.cookie = a + "=" + b + e + d + c + f
    };
    h.get = function(a, b) {
        for (var c = a + "=", d = (this.g.cookie || "").split(";"), e = 0, f; e < d.length; e++) {
            f = Db(d[e]);
            if (0 == f.lastIndexOf(c, 0)) return f.substr(c.length);
            if (f == a) return ""
        }
        return b
    };
    h.Ra = function() {
        return Fe(this).keys
    };
    h.oa = function() {
        return Fe(this).values
    };
    h.isEmpty = function() {
        return !this.g.cookie
    };
    h.Ga = function() {
        return this.g.cookie ? (this.g.cookie || "").split(";").length : 0
    };
    h.clear = function() {
        for (var a = Fe(this).keys, b = a.length - 1; 0 <= b; b--) {
            var c = a[b];
            this.get(c);
            this.set(c, "", 0, void 0, void 0)
        }
    };
    var Fe = function(a) {
            a = (a.g.cookie || "").split(";");
            for (var b = [], c = [], d, e, f = 0; f < a.length; f++) e = Db(a[f]), d = e.indexOf("="), -1 == d ? (b.push(""), c.push(e)) : (b.push(e.substring(0, d)), c.push(e.substring(d + 1)));
            return {
                keys: b,
                values: c
            }
        },
        Ge = new Ee("undefined" == typeof document ? null : document);
    Ge.h = 3950;
    var He = /^(?:([^:/?#.]+):)?(?:\/\/(?:([^/?#]*)@)?([^/#?]*?)(?::([0-9]+))?(?=[/#?]|$))?([^?#]+)?(?:\?([^#]*))?(?:#([\s\S]*))?$/,
        Ie = function(a, b) {
            if (a) {
                a = a.split("&");
                for (var c = 0; c < a.length; c++) {
                    var d = a[c].indexOf("="),
                        e = null;
                    if (0 <= d) {
                        var f = a[c].substring(0, d);
                        e = a[c].substring(d + 1)
                    } else f = a[c];
                    b(f, e ? Eb(e) : "")
                }
            }
        },
        Je = /#|$/,
        Ke = function(a, b) {
            var c = a.search(Je);
            a: {
                var d = 0;
                for (var e = b.length; 0 <= (d = a.indexOf(b, d)) && d < c;) {
                    var f = a.charCodeAt(d - 1);
                    if (38 == f || 63 == f)
                        if (f = a.charCodeAt(d + e), !f || 61 == f || 38 == f || 35 == f) break a;
                    d += e + 1
                }
                d = -1
            }
            if (0 > d) return null;
            e = a.indexOf("&", d);
            if (0 > e || e > c) e = c;
            d += b.length + 1;
            return Eb(a.substr(d, e - d))
        };
    var Le = function() {
        this.g = {};
        return this
    };
    Le.prototype.set = function(a, b) {
        this.g[a] = b
    };
    var Me = function(a, b) {
        a.g.eb = bb(a.g, "eb", 0) | b
    };
    Le.prototype.get = function(a) {
        return bb(this.g, a, null)
    };
    var I = function(a, b, c, d) {
        this.top = a;
        this.right = b;
        this.bottom = c;
        this.left = d
    };
    I.prototype.h = function() {
        return this.right - this.left
    };
    I.prototype.g = function() {
        return this.bottom - this.top
    };
    I.prototype.clone = function() {
        return new I(this.top, this.right, this.bottom, this.left)
    };
    I.prototype.contains = function(a) {
        return this && a ? a instanceof I ? a.left >= this.left && a.right <= this.right && a.top >= this.top && a.bottom <= this.bottom : a.x >= this.left && a.x <= this.right && a.y >= this.top && a.y <= this.bottom : !1
    };
    var Ne = function(a, b) {
        return a == b ? !0 : a && b ? a.top == b.top && a.right == b.right && a.bottom == b.bottom && a.left == b.left : !1
    };
    I.prototype.ceil = function() {
        this.top = Math.ceil(this.top);
        this.right = Math.ceil(this.right);
        this.bottom = Math.ceil(this.bottom);
        this.left = Math.ceil(this.left);
        return this
    };
    I.prototype.floor = function() {
        this.top = Math.floor(this.top);
        this.right = Math.floor(this.right);
        this.bottom = Math.floor(this.bottom);
        this.left = Math.floor(this.left);
        return this
    };
    I.prototype.round = function() {
        this.top = Math.round(this.top);
        this.right = Math.round(this.right);
        this.bottom = Math.round(this.bottom);
        this.left = Math.round(this.left);
        return this
    };
    var Oe = function(a, b, c) {
        b instanceof Zc ? (a.left += b.x, a.right += b.x, a.top += b.y, a.bottom += b.y) : (a.left += b, a.right += b, r(c) && (a.top += c, a.bottom += c));
        return a
    };
    I.prototype.scale = function(a, b) {
        b = r(b) ? b : a;
        this.left *= a;
        this.right *= a;
        this.top *= b;
        this.bottom *= b;
        return this
    };
    var Pe = function(a, b, c, d) {
        this.left = a;
        this.top = b;
        this.width = c;
        this.height = d
    };
    Pe.prototype.clone = function() {
        return new Pe(this.left, this.top, this.width, this.height)
    };
    var Qe = function(a) {
        return new I(a.top, a.left + a.width, a.top + a.height, a.left)
    };
    h = Pe.prototype;
    h.contains = function(a) {
        return a instanceof Zc ? a.x >= this.left && a.x <= this.left + this.width && a.y >= this.top && a.y <= this.top + this.height : this.left <= a.left && this.left + this.width >= a.left + a.width && this.top <= a.top && this.top + this.height >= a.top + a.height
    };
    h.ceil = function() {
        this.left = Math.ceil(this.left);
        this.top = Math.ceil(this.top);
        this.width = Math.ceil(this.width);
        this.height = Math.ceil(this.height);
        return this
    };
    h.floor = function() {
        this.left = Math.floor(this.left);
        this.top = Math.floor(this.top);
        this.width = Math.floor(this.width);
        this.height = Math.floor(this.height);
        return this
    };
    h.round = function() {
        this.left = Math.round(this.left);
        this.top = Math.round(this.top);
        this.width = Math.round(this.width);
        this.height = Math.round(this.height);
        return this
    };
    h.scale = function(a, b) {
        b = r(b) ? b : a;
        this.left *= a;
        this.width *= a;
        this.top *= b;
        this.height *= b;
        return this
    };
    var Re = null,
        Se = function() {
            this.g = {};
            this.h = 0
        },
        Te = function(a, b) {
            this.A = a;
            this.o = !0;
            this.h = b
        };
    Te.prototype.g = function() {
        return this.h
    };
    Te.prototype.l = function() {
        return String(this.h)
    };
    var Ue = function(a, b) {
        Te.call(this, String(a), b);
        this.v = a;
        this.h = !!b
    };
    y(Ue, Te);
    Ue.prototype.l = function() {
        return this.h ? "1" : "0"
    };
    var Ve = function(a, b) {
        Te.call(this, a, b)
    };
    y(Ve, Te);
    Ve.prototype.l = function() {
        return this.h ? Math.round(this.h.top) + "." + Math.round(this.h.left) + "." + (Math.round(this.h.top) + Math.round(this.h.height)) + "." + (Math.round(this.h.left) + Math.round(this.h.width)) : ""
    };
    var We = function(a) {
            if (a.match(/^-?[0-9]+\.-?[0-9]+\.-?[0-9]+\.-?[0-9]+$/)) {
                a = a.split(".");
                var b = Number(a[0]),
                    c = Number(a[1]);
                return new Ve("", new Pe(c, b, Number(a[3]) - c, Number(a[2]) - b))
            }
            return new Ve("", new Pe(0, 0, 0, 0))
        },
        Xe = function() {
            Re || (Re = new Se);
            return Re
        },
        Ye = function(a, b) {
            a.g[b.A] = b
        };
    var $e = function(a, b) {
            if (q(b))(b = Ze(a, b)) && (a.style[b] = void 0);
            else
                for (var c in b) {
                    var d = a,
                        e = b[c],
                        f = Ze(d, c);
                    f && (d.style[f] = e)
                }
        },
        af = {},
        Ze = function(a, b) {
            var c = af[b];
            if (!c) {
                var d = Vb(b);
                c = d;
                void 0 === a.style[d] && (d = (sc ? "Webkit" : rc ? "Moz" : pc ? "ms" : oc ? "O" : null) + Xb(d), void 0 !== a.style[d] && (c = d));
                af[b] = c
            }
            return c
        },
        bf = function(a, b) {
            var c = a.style[Vb(b)];
            return "undefined" !== typeof c ? c : a.style[Ze(a, b)] || ""
        },
        cf = function(a) {
            try {
                var b = a.getBoundingClientRect()
            } catch (c) {
                return {
                    left: 0,
                    top: 0,
                    right: 0,
                    bottom: 0
                }
            }
            pc && a.ownerDocument.body &&
                (a = a.ownerDocument, b.left -= a.documentElement.clientLeft + a.body.clientLeft, b.top -= a.documentElement.clientTop + a.body.clientTop);
            return b
        },
        df = function(a) {
            var b = bd(a),
                c = new Zc(0, 0);
            var d = b ? bd(b) : document;
            d = !pc || 9 <= Number(Ec) || "CSS1Compat" == cd(d).g.compatMode ? d.documentElement : d.body;
            if (a == d) return c;
            a = cf(a);
            b = jd(cd(b).g);
            c.x = a.left + b.x;
            c.y = a.top + b.y;
            return c
        },
        ef = function(a, b) {
            var c = new Zc(0, 0),
                d = F(bd(a));
            if (!lc(d, "parent")) return c;
            do {
                if (d == b) var e = df(a);
                else e = cf(a), e = new Zc(e.left, e.top);
                c.x += e.x;
                c.y += e.y
            } while (d && d != b && d != d.parent && (a = d.frameElement) && (d = d.parent));
            return c
        },
        ff = function(a) {
            var b = a.offsetWidth,
                c = a.offsetHeight,
                d = sc && !b && !c;
            return p(b) && !d || !a.getBoundingClientRect ? new E(b, c) : (a = cf(a), new E(a.right - a.left, a.bottom - a.top))
        };
    var gf = function(a) {
            var b = new Pe(-Number.MAX_VALUE / 2, -Number.MAX_VALUE / 2, Number.MAX_VALUE, Number.MAX_VALUE),
                c = new Pe(0, 0, 0, 0);
            if (!a || 0 == a.length) return c;
            for (var d = 0; d < a.length; d++) {
                a: {
                    var e = b;
                    var f = a[d],
                        g = Math.max(e.left, f.left),
                        k = Math.min(e.left + e.width, f.left + f.width);
                    if (g <= k) {
                        var m = Math.max(e.top, f.top);
                        f = Math.min(e.top + e.height, f.top + f.height);
                        if (m <= f) {
                            e.left = g;
                            e.top = m;
                            e.width = k - g;
                            e.height = f - m;
                            e = !0;
                            break a
                        }
                    }
                    e = !1
                }
                if (!e) return c
            }
            return b
        },
        hf = function(a, b) {
            var c = a.getBoundingClientRect();
            a = ef(a,
                b);
            return new Pe(Math.round(a.x), Math.round(a.y), Math.round(c.right - c.left), Math.round(c.bottom - c.top))
        },
        jf = function(a, b, c) {
            if (b && c) {
                a: {
                    var d = Math.max(b.left, c.left);
                    var e = Math.min(b.left + b.width, c.left + c.width);
                    if (d <= e) {
                        var f = Math.max(b.top, c.top),
                            g = Math.min(b.top + b.height, c.top + c.height);
                        if (f <= g) {
                            d = new Pe(d, f, e - d, g - f);
                            break a
                        }
                    }
                    d = null
                }
                e = d ? d.height * d.width : 0;f = d ? b.height * b.width : 0;d = d && f ? Math.round(e / f * 100) : 0;Ye(a, new Te("vp", d));d && 0 < d ? (e = Qe(b), f = Qe(c), e = e.top >= f.top && e.top < f.bottom) : e = !1;Ye(a, new Ue(512,
                    e));d && 0 < d ? (e = Qe(b), f = Qe(c), e = e.bottom <= f.bottom && e.bottom > f.top) : e = !1;Ye(a, new Ue(1024, e));d && 0 < d ? (e = Qe(b), f = Qe(c), e = e.left >= f.left && e.left < f.right) : e = !1;Ye(a, new Ue(2048, e));d && 0 < d ? (b = Qe(b), c = Qe(c), c = b.right <= c.right && b.right > c.left) : c = !1;Ye(a, new Ue(4096, c))
            }
        };
    var kf = function(a, b) {
        var c = 0;
        Wa(F(), "ima", "video", "client", "tagged") && (c = 1);
        var d = null;
        a && (d = a());
        if (d) {
            a = Xe();
            a.g = {};
            var e = new Ue(32, !0);
            e.o = !1;
            Ye(a, e);
            e = F().document;
            e = e.visibilityState || e.webkitVisibilityState || e.mozVisibilityState || e.msVisibilityState || "";
            Ye(a, new Ue(64, "hidden" != e.toLowerCase().substring(e.length - 6) ? !0 : !1));
            try {
                var f = F().top;
                try {
                    var g = !!f.location.href || "" === f.location.href
                } catch (t) {
                    g = !1
                }
                if (g) {
                    var k = sd(d);
                    var m = k && 0 != k.length ? "1" : "0"
                } else m = "2"
            } catch (t) {
                m = "2"
            }
            Ye(a, new Ue(256,
                "2" == m));
            Ye(a, new Ue(128, "1" == m));
            k = g = F().top;
            "2" == m && (k = F());
            f = hf(d, k);
            Ye(a, new Ve("er", f));
            try {
                var l = k.document && !k.document.body ? null : id(k || window)
            } catch (t) {
                l = null
            }
            l ? (k = jd(cd(k.document).g), Ye(a, new Ue(16384, !!k)), l = k ? new Pe(k.x, k.y, l.width, l.height) : null) : l = null;
            Ye(a, new Ve("vi", l));
            if (l && "1" == m) {
                m = sd(d);
                d = [];
                for (k = 0; k < m.length; k++)(e = hf(m[k], g)) && d.push(e);
                d.push(l);
                l = gf(d)
            }
            jf(a, f, l);
            a.h && (m = ge() - a.h, Ye(a, new Te("ts", m)));
            a.h = ge()
        } else a = Xe(), a.g = {}, a.h = ge(), Ye(a, new Ue(32, !1));
        this.l = a;
        this.g =
            new Le;
        this.g.set("ve", 4);
        c && Me(this.g, 1);
        Wa(F(), "ima", "video", "client", "crossdomainTag") && Me(this.g, 4);
        Wa(F(), "ima", "video", "client", "sdkTag") && Me(this.g, 8);
        Wa(F(), "ima", "video", "client", "jsTag") && Me(this.g, 2);
        b && bb(b, "fullscreen", !1) && Me(this.g, 16);
        this.h = b = null;
        if (c && (c = Wa(F(), "ima", "video", "client"), c.getEData)) {
            this.h = c.getEData();
            if (c = Wa(F(), "ima", "video", "client", "getLastSnapshotFromTop"))
                if (a = c()) this.h.extendWithDataFromTopIframe(a.tagstamp, a.playstamp, a.lactstamp), c = this.l, b = a.er, a = a.vi,
                    b && a && (b = We(b).g(), a = We(a).g(), m = null, bb(c.g, "er", null) && (m = bb(c.g, "er", null).g(), m.top += b.top, m.left += b.left, Ye(c, new Ve("er", m))), bb(c.g, "vi", null) && (l = bb(c.g, "vi", null).g(), l.top += b.top, l.left += b.left, d = [], d.push(l), d.push(b), d.push(a), b = gf(d), jf(c, m, b), Ye(c, new Ve("vi", a))));
            a: {
                if (this.h) {
                    if (this.h.getTagLoadTimestamp) {
                        b = this.h.getTagLoadTimestamp();
                        break a
                    }
                    if (this.h.getTimeSinceTagLoadSeconds) {
                        b = this.h.getTimeSinceTagLoadSeconds();
                        break a
                    }
                }
                b = null
            }
        }
        this.g.set("td", ge() - he(b))
    };
    var lf = new ee(200),
        mf = function(a, b) {
            try {
                var c = new kf(a, b);
                a = [];
                var d = Number(c.g.get("eb")),
                    e = c.g.g;
                "eb" in e && delete e.eb;
                var f, g = c.g;
                e = [];
                for (var k in g.g) e.push(k + g.g[k]);
                (f = e.join("_")) && a.push(f);
                if (c.h) {
                    var m = c.h.serialize();
                    m && a.push(m)
                }
                var l, t = c.l;
                f = d;
                g = [];
                f || (f = 0);
                for (var H in t.g) {
                    var fa = t.g[H];
                    if (fa instanceof Ue) fa.g() && (f |= fa.v);
                    else {
                        var va, La = t.g[H];
                        (va = La.o ? La.l() : "") && g.push(H + va)
                    }
                }
                g.push("eb" + String(f));
                (l = g.join("_")) && a.push(l);
                c.g.set("eb", d);
                return a.join("_")
            } catch (V) {
                return "tle;" +
                    Nb(V.name, 12) + ";" + Nb(V.message, 40)
            }
        },
        nf = function(a, b) {
            Pd(lf, "tick", function() {
                var c = mf(b);
                a(c)
            });
            lf.start();
            lf.dispatchEvent("tick")
        };
    var of = function() {}, pf = "function" == typeof Uint8Array, qf = [], rf = function(a, b) {
        if (b < a.l) {
            b += a.v;
            var c = a.g[b];
            return c === qf ? a.g[b] = [] : c
        }
        if (a.h) return c = a.h[b], c === qf ? a.h[b] = [] : c
    }, sf = function(a, b) {
        if (b < a.l) {
            b += a.v;
            var c = a.g[b];
            return c === qf ? a.g[b] = [] : c
        }
        c = a.h[b];
        return c === qf ? a.h[b] = [] : c
    }, uf = function(a) {
        if (a.o)
            for (var b in a.o) {
                var c = a.o[b];
                if (Ga(c))
                    for (var d = 0; d < c.length; d++) c[d] && tf(c[d]);
                else c && tf(c)
            }
    }, tf = function(a) {
        uf(a);
        return a.g
    }; of .prototype.toString = function() {
        uf(this);
        return this.g.toString()
    }; of .prototype.clone = function() {
        return new this.constructor(vf(tf(this)))
    };
    var vf = function(a) {
        if (Ga(a)) {
            for (var b = Array(a.length), c = 0; c < a.length; c++) {
                var d = a[c];
                null != d && (b[c] = "object" == typeof d ? vf(d) : d)
            }
            return b
        }
        if (pf && a instanceof Uint8Array) return new Uint8Array(a);
        b = {};
        for (c in a) d = a[c], null != d && (b[c] = "object" == typeof d ? vf(d) : d);
        return b
    };
    var wf = document,
        J = window;
    var yf = function(a) {
        var b = a;
        a = xf;
        this.o = null;
        b || (b = []);
        this.v = -1;
        this.g = b;
        a: {
            if (b = this.g.length) {
                --b;
                var c = this.g[b];
                if (c && "object" == typeof c && !Ga(c) && !(pf && c instanceof Uint8Array)) {
                    this.l = b - -1;
                    this.h = c;
                    break a
                }
            }
            this.l = Number.MAX_VALUE
        }
        if (a)
            for (b = 0; b < a.length; b++)
                if (c = a[b], c < this.l) c += -1, this.g[c] = this.g[c] || qf;
                else {
                    var d = this.l + -1;
                    this.g[d] || (this.h = this.g[d] = {});
                    this.h[c] = this.h[c] || qf
                }
    };
    y(yf, of );
    var xf = [1, 2, 3, 4];
    var zf = function() {
        this.g = new Ee(document)
    };
    zf.prototype.get = function(a) {
        a = this.g.get(a);
        return void 0 === a ? null : a
    };
    zf.prototype.set = function(a, b) {
        this.g.set(a, b, 0, "", "")
    };
    var Af = function() {
        var a = new zf;
        try {
            var b = a.get("DATA_USE_CONSENT")
        } catch (c) {}
        if (!b) return null;
        try {
            return new yf(b ? JSON.parse(b) : null)
        } catch (c) {
            return null
        }
    };
    var Cf = function(a) {
            Bf();
            return Pc(a)
        },
        Bf = Da;
    var Df = function(a) {
            try {
                return !!a && null != a.location.href && lc(a, "foo")
            } catch (b) {
                return !1
            }
        },
        Ef = function(a, b) {
            if (a)
                for (var c in a) Object.prototype.hasOwnProperty.call(a, c) && b.call(void 0, a[c], c, a)
        },
        Gf = function() {
            var a = [];
            Ef(Ff, function(b) {
                a.push(b)
            });
            return a
        },
        Hf = /https?:\/\/[^\/]+/,
        If = function(a) {
            return (a = Hf.exec(a)) && a[0] || ""
        },
        Jf = function() {
            var a = n;
            try {
                for (var b = null; b != a; b = a, a = a.parent) switch (a.location.protocol) {
                    case "https:":
                        return !0;
                    case "file:":
                        return !1;
                    case "http:":
                        return !1
                }
            } catch (c) {}
            return !0
        },
        Sf = function() {
            var a = Rf;
            if (!a) return "";
            var b = /.*[&#?]google_debug(=[^&]*)?(&.*)?$/;
            try {
                var c = b.exec(decodeURIComponent(a));
                if (c) return c[1] && 1 < c[1].length ? c[1].substring(1) : "true"
            } catch (d) {}
            return ""
        },
        Tf = function(a, b) {
            try {
                return !(!a.frames || !a.frames[b])
            } catch (c) {
                return !1
            }
        };
    var Uf = de(function() {
        var a = !1;
        try {
            var b = Object.defineProperty({}, "passive", {
                get: function() {
                    a = !0
                }
            });
            n.addEventListener("test", null, b)
        } catch (c) {}
        return a
    });

    function Vf(a) {
        return a ? a.passive && Uf() ? a : a.capture || !1 : a
    }
    var Wf = function(a, b, c, d) {
            a.addEventListener ? a.addEventListener(b, c, Vf(d)) : a.attachEvent && a.attachEvent("on" + b, c)
        },
        Xf = function(a, b, c) {
            a.removeEventListener ? a.removeEventListener(b, c, Vf(void 0)) : a.detachEvent && a.detachEvent("on" + b, c)
        };
    var Yf = function(a, b, c) {
        var d = !1,
            e = !1;
        e = void 0 === e ? !1 : e;
        d = void 0 === d ? !1 : d;
        a.google_image_requests || (a.google_image_requests = []);
        var f = a.document.createElement("img");
        if (c || d) {
            var g = function(b) {
                c && c(b);
                d && tb(a.google_image_requests, f);
                Xf(f, "load", g);
                Xf(f, "error", g)
            };
            Wf(f, "load", g);
            Wf(f, "error", g)
        }
        e && (f.referrerPolicy = "no-referrer");
        f.src = b;
        a.google_image_requests.push(f)
    };
    var Zf = function(a) {
        var b = Af();
        if (!b) return 0;
        if (rf(b, 7)) return 4;
        if (6048E5 < x() - rf(b, 5)) return 0;
        if (a) {
            if (rb(sf(b, 3), a)) return 2;
            if (rb(sf(b, 4), a)) return 3
        }
        return 1
    };
    var $f = function(a) {
        a = void 0 === a ? n : a;
        var b = a.context || a.AMP_CONTEXT_DATA;
        if (!b) try {
            b = a.parent.context || a.parent.AMP_CONTEXT_DATA
        } catch (c) {}
        try {
            if (b && b.pageViewId && b.canonicalUrl) return b
        } catch (c) {}
        return null
    };
    var ag = !!window.google_async_iframe_id,
        bg = ag && window.parent || window,
        cg = function() {
            if (ag && !Df(bg)) {
                var a = "." + wf.domain;
                try {
                    for (; 2 < a.split(".").length && !Df(bg);) wf.domain = a = a.substr(a.indexOf(".") + 1), bg = window.parent
                } catch (b) {}
                Df(bg) || (bg = window)
            }
            return bg
        };
    var dg = function(a, b, c) {
        a && null !== b && b != b.top && (b = b.top);
        try {
            return (void 0 === c ? 0 : c) ? (new E(b.innerWidth, b.innerHeight)).round() : id(b || window).round()
        } catch (d) {
            return new E(-12245933, -12245933)
        }
    };
    var eg = function(a) {
        var b = {};
        z(a, function(a) {
            var c = a.event,
                e = b[c];
            b.hasOwnProperty(c) ? null === e || a.g(e) || (b[c] = null) : b[c] = a
        });
        ub(a, function(a) {
            return null === b[a.event]
        })
    };
    var fg = {
            NONE: 0,
            Hg: 1
        },
        gg = {
            li: 1
        };
    var hg = function() {
            this.g = 0;
            this.l = !1;
            this.o = -1;
            this.Wa = !1;
            this.h = 0
        },
        ig = function(a) {
            return a.Wa ? .3 <= a.g : .5 <= a.g
        };
    var jg = {
            zd: 0,
            Ng: 1
        },
        kg = {
            wh: 0,
            th: 1,
            uh: 2
        },
        lg = {
            370204044: 0,
            370204045: 1
        },
        mg = {
            370204032: 0,
            370204033: 1
        },
        ng = {
            370204034: 0,
            370204035: 1,
            370204038: 0,
            370204039: 1
        },
        og = {
            370204028: 0,
            370204029: 1,
            370204040: 0,
            370204041: 1
        },
        pg = {
            953563515: 0,
            953563516: 1,
            953563517: 2
        },
        qg = {
            370204018: 0,
            370204019: 1,
            370204026: 0,
            370204027: 1
        },
        rg = {
            668123008: 0,
            668123009: 1
        },
        sg = {
            668123028: 0,
            668123029: 1
        },
        tg = {
            NONE: 0,
            fh: 1
        },
        ug = {
            480596784: 0,
            480596785: 1
        },
        vg = {
            zd: 0,
            ih: 1,
            hh: 2
        },
        wg = {
            21061799: 0,
            21061800: 1,
            21061801: 2
        };
    var xg = function(a) {
            this.v = a;
            this.h = null;
            this.l = !1;
            this.o = null
        },
        K = function(a) {
            a.l = !0;
            return a
        },
        yg = function(a, b) {
            a.o = void 0 === b ? null : b
        },
        zg = function(a, b) {
            a.o && z(b, function(b) {
                b = a.o[b];
                void 0 !== b && null === a.h && Ya(a.v, b) && (a.h = b)
            })
        };
    xg.prototype.g = function() {
        return this.h
    };
    var Ag = function() {
        this.g = {};
        this.l = !0;
        this.h = {}
    };
    Ag.prototype.reset = function() {
        this.g = {};
        this.l = !0;
        this.h = {}
    };
    var M = function(a, b, c) {
            a.g[b] || (a.g[b] = new xg(c));
            return a.g[b]
        },
        Bg = function(a, b, c) {
            (a = a.g[b]) && null === a.h && Ya(a.v, c) && (a.h = c)
        },
        Cg = function(a, b) {
            if (Xa(a.h, b)) return a.h[b];
            if (a = a.g[b]) return a.g()
        },
        Dg = function(a) {
            var b = {},
                c = Qa(a.g, function(a) {
                    return a.l
                });
            Pa(c, function(c, e) {
                c = void 0 !== a.h[e] ? String(a.h[e]) : c.l && null !== c.h ? String(c.h) : "";
                0 < c.length && (b[e] = c)
            }, a);
            return b
        },
        Eg = function(a) {
            a = Dg(a);
            var b = [];
            Pa(a, function(a, d) {
                d in Object.prototype || "undefined" != typeof a && b.push([d, ":", a].join(""))
            });
            return b
        },
        Fg = function(a) {
            var b = N.C().M;
            b.l && z(Ua(b.g), function(b) {
                return zg(b, a)
            })
        };
    var Gg = x(),
        Hg = -1,
        Ig = -1,
        Jg, Kg = -1,
        Lg = !1,
        O = function() {
            return x() - Gg
        },
        Mg = function(a) {
            var b = 0 <= Ig ? O() - Ig : -1,
                c = Lg ? O() - Hg : -1,
                d = 0 <= Kg ? O() - Kg : -1;
            if (79463068 == a) return 500;
            if (947190542 == a) return 100;
            if (79463069 == a) return 200;
            a = [2E3, 4E3];
            var e = [250, 500, 1E3];
            var f = b; - 1 != c && c < b && (f = c);
            for (b = 0; b < a.length; ++b)
                if (f < a[b]) {
                    var g = e[b];
                    break
                }
            void 0 === g && (g = e[a.length]);
            return -1 != d && 1500 < d && 4E3 > d ? 500 : g
        };
    var Ng = function(a, b) {
            this.h = (void 0 === a ? 0 : a) || 0;
            this.g = (void 0 === b ? "" : b) || ""
        },
        Og = function() {
            var a = N.C().A;
            return !!a.h || !!a.g
        };
    Ng.prototype.toString = function() {
        return this.h + (this.g ? "-" : "") + this.g
    };
    Ng.prototype.matches = function(a) {
        return this.g || a.g ? this.g == a.g : this.h || a.h ? this.h == a.h : !1
    };
    var Pg = function(a, b, c) {
        c = void 0 === c ? {} : c;
        this.error = a;
        this.context = b.context;
        this.line = b.line || -1;
        this.msg = b.message || "";
        this.file = b.file || "";
        this.id = b.id || "jserror";
        this.meta = c
    };
    var Qg = /^https?:\/\/(\w|-)+\.cdn\.ampproject\.(net|org)(\?|\/|$)/,
        Ug = function(a) {
            a = a || Rg();
            for (var b = new Sg(n.location.href, n, !1), c = null, d = a.length - 1, e = d; 0 <= e; --e) {
                var f = a[e];
                !c && Qg.test(f.url) && (c = f);
                if (f.url && !f.oc) {
                    b = f;
                    break
                }
            }
            e = null;
            f = a.length && a[d].url;
            0 != b.depth && f && (e = a[d]);
            return new Tg(b, e, c)
        },
        Rg = function() {
            var a = n,
                b = [],
                c = null;
            do {
                var d = a;
                if (Df(d)) {
                    var e = d.location.href;
                    c = d.document && d.document.referrer || null
                } else e = c, c = null;
                b.push(new Sg(e || "", d));
                try {
                    a = d.parent
                } catch (f) {
                    a = null
                }
            } while (a &&
                d != a);
            d = 0;
            for (a = b.length - 1; d <= a; ++d) b[d].depth = a - d;
            d = n;
            if (d.location && d.location.ancestorOrigins && d.location.ancestorOrigins.length == b.length - 1)
                for (a = 1; a < b.length; ++a) e = b[a], e.url || (e.url = d.location.ancestorOrigins[a - 1] || "", e.oc = !0);
            return b
        },
        Tg = function(a, b, c) {
            this.g = a;
            this.h = b;
            this.l = c
        },
        Sg = function(a, b, c) {
            this.url = a;
            this.aa = b;
            this.oc = !!c;
            this.depth = r(void 0) ? void 0 : null
        };
    var Vg = function() {
            this.l = "&";
            this.o = p(void 0) ? void 0 : "trn";
            this.v = !1;
            this.h = {};
            this.A = 0;
            this.g = []
        },
        Wg = function(a, b) {
            var c = {};
            c[a] = b;
            return [c]
        },
        Yg = function(a, b, c, d, e) {
            var f = [];
            Ef(a, function(a, k) {
                (a = Xg(a, b, c, d, e)) && f.push(k + "=" + a)
            });
            return f.join(b)
        },
        Xg = function(a, b, c, d, e) {
            if (null == a) return "";
            b = b || "&";
            c = c || ",$";
            "string" == typeof c && (c = c.split(""));
            if (a instanceof Array) {
                if (d = d || 0, d < c.length) {
                    for (var f = [], g = 0; g < a.length; g++) f.push(Xg(a[g], b, c, d + 1, e));
                    return f.join(c[d])
                }
            } else if ("object" == typeof a) return e =
                e || 0, 2 > e ? encodeURIComponent(Yg(a, b, c, d, e + 1)) : "...";
            return encodeURIComponent(String(a))
        },
        Zg = function(a, b, c, d) {
            a.g.push(b);
            a.h[b] = Wg(c, d)
        },
        ah = function(a, b, c, d) {
            b = b + "//" + c + d;
            var e = $g(a) - d.length;
            if (0 > e) return "";
            a.g.sort(function(a, b) {
                return a - b
            });
            d = null;
            c = "";
            for (var f = 0; f < a.g.length; f++)
                for (var g = a.g[f], k = a.h[g], m = 0; m < k.length; m++) {
                    if (!e) {
                        d = null == d ? g : d;
                        break
                    }
                    var l = Yg(k[m], a.l, ",$");
                    if (l) {
                        l = c + l;
                        if (e >= l.length) {
                            e -= l.length;
                            b += l;
                            c = a.l;
                            break
                        } else a.v && (c = e, l[c - 1] == a.l && --c, b += l.substr(0, c), c = a.l, e = 0);
                        d =
                            null == d ? g : d
                    }
                }
            f = "";
            a.o && null != d && (f = c + a.o + "=" + d);
            return b + f + ""
        },
        $g = function(a) {
            if (!a.o) return 4E3;
            var b = 1,
                c;
            for (c in a.h) b = c.length > b ? c.length : b;
            return 4E3 - a.o.length - b - a.l.length - 1
        };
    var bh = function(a, b, c, d, e) {
        if (Math.random() < (d || a.g)) try {
            if (c instanceof Vg) var f = c;
            else f = new Vg, Ef(c, function(a, b) {
                var c = f,
                    d = c.A++;
                a = Wg(b, a);
                c.g.push(d);
                c.h[d] = a
            });
            var g = ah(f, a.o, a.h, a.l + b + "&");
            g && ("undefined" === typeof e ? Yf(n, g, void 0) : Yf(n, g, e))
        } catch (k) {}
    };
    var ch = null,
        dh = function(a) {
            this.h = {};
            this.g = {};
            a = a || [];
            for (var b = 0, c = a.length; b < c; ++b) this.g[a[b]] = ""
        },
        fh = function() {
            var a = eh(),
                b = new dh;
            Ef(a.h, function(a, d) {
                b.h[d] = a
            });
            Ef(a.g, function(a, d) {
                b.g[d] = a
            });
            return b
        };
    var gh = {
        ni: 0,
        sh: 1,
        Th: 2,
        Mg: 3,
        ji: 4,
        Tg: 5,
        lh: 6,
        Uh: 7,
        ug: 8,
        Xg: 9,
        wg: 10,
        Zh: 11,
        Wh: 12
    };
    var hh = function() {
            var a = n.performance;
            return a && a.now && a.timing ? Math.floor(a.now() + a.timing.navigationStart) : x()
        },
        ih = function() {
            var a = void 0 === a ? n : a;
            return (a = a.performance) && a.now ? a.now() : null
        };
    var jh = function(a, b, c, d, e) {
        this.label = a;
        this.type = b;
        this.value = c;
        this.duration = void 0 === d ? 0 : d;
        this.uniqueId = this.label + "_" + this.type + "_" + Math.random();
        this.slotId = e
    };
    var kh = n.performance,
        lh = !!(kh && kh.mark && kh.measure && kh.clearMarks),
        mh = de(function() {
            var a;
            if (a = lh) {
                var b;
                if (null === ch) {
                    ch = "";
                    try {
                        a = "";
                        try {
                            a = n.top.location.hash
                        } catch (c) {
                            a = n.location.hash
                        }
                        a && (ch = (b = a.match(/\bdeid=([\d,]+)/)) ? b[1] : "")
                    } catch (c) {}
                }
                b = ch;
                a = !!b.indexOf && 0 <= b.indexOf("1337")
            }
            return a
        }),
        nh = function(a, b) {
            this.events = [];
            this.g = b || n;
            var c = null;
            b && (b.google_js_reporting_queue = b.google_js_reporting_queue || [], this.events = b.google_js_reporting_queue, c = b.google_measure_js_timing);
            this.h = mh() || (null !=
                c ? c : Math.random() < a)
        };
    nh.prototype.v = function() {
        this.h = !1;
        this.events != this.g.google_js_reporting_queue && (mh() && z(this.events, oh), this.events.length = 0)
    };
    nh.prototype.B = function(a) {
        this.h && this.events.push(a)
    };
    var oh = function(a) {
        a && kh && mh() && (kh.clearMarks("goog_" + a.uniqueId + "_start"), kh.clearMarks("goog_" + a.uniqueId + "_end"))
    };
    nh.prototype.start = function(a, b) {
        if (!this.h) return null;
        var c = ih() || hh();
        a = new jh(a, b, c);
        b = "goog_" + a.uniqueId + "_start";
        kh && mh() && kh.mark(b);
        return a
    };
    nh.prototype.end = function(a) {
        if (this.h && r(a.value)) {
            var b = ih() || hh();
            a.duration = b - a.value;
            b = "goog_" + a.uniqueId + "_end";
            kh && mh() && kh.mark(b);
            this.B(a)
        }
    };
    var rh = function() {
        var a = ph;
        this.o = qh;
        this.l = !0;
        this.h = null;
        this.v = this.Ka;
        this.g = void 0 === a ? null : a
    };
    h = rh.prototype;
    h.qd = function(a) {
        this.h = a
    };
    h.rd = function(a) {
        this.l = a
    };
    h.Ob = function(a, b, c, d) {
        try {
            if (this.g && this.g.h) {
                var e = this.g.start(a.toString(), 3);
                var f = b();
                this.g.end(e)
            } else f = b()
        } catch (k) {
            b = this.l;
            try {
                oh(e);
                var g = sh(k);
                b = (d || this.v).call(this, a, g, void 0, c)
            } catch (m) {
                this.Ka(217, m)
            }
            if (!b) throw k;
        }
        return f
    };
    h.jd = function(a, b, c, d, e) {
        var f = this;
        return function(g) {
            for (var k = [], m = 0; m < arguments.length; ++m) k[m - 0] = arguments[m];
            return f.Ob(a, function() {
                return b.apply(c, k)
            }, d, e)
        }
    };
    h.Ka = function(a, b, c, d, e) {
        e = e || "jserror";
        try {
            var f = new Vg;
            f.v = !0;
            Zg(f, 1, "context", a);
            b.error && b.meta && b.id || (b = sh(b));
            b.msg && Zg(f, 2, "msg", b.msg.substring(0, 512));
            b.file && Zg(f, 3, "file", b.file);
            0 < b.line && Zg(f, 4, "line", b.line);
            var g = b.meta || {};
            if (this.h) try {
                this.h(g)
            } catch (m) {}
            if (d) try {
                d(g)
            } catch (m) {}
            b = [g];
            f.g.push(5);
            f.h[5] = b;
            var k = Ug();
            k.h && Zg(f, 6, "top", k.h.url || "");
            Zg(f, 7, "url", k.g.url || "");
            bh(this.o, e, f, c)
        } catch (m) {
            try {
                bh(this.o, e, {
                    context: "ecmserr",
                    rctx: a,
                    msg: th(m),
                    url: k && k.g.url
                }, c)
            } catch (l) {}
        }
        return this.l
    };
    var sh = function(a) {
            return new uh(th(a), a.fileName, a.lineNumber)
        },
        th = function(a) {
            var b = a.toString();
            a.name && -1 == b.indexOf(a.name) && (b += ": " + a.name);
            a.message && -1 == b.indexOf(a.message) && (b += ": " + a.message);
            if (a.stack) {
                a = a.stack;
                var c = b;
                try {
                    -1 == a.indexOf(c) && (a = c + "\n" + a);
                    for (var d; a != d;) d = a, a = a.replace(/((https?:\/..*\/)[^\/:]*:\d+(?:.|\n)*)\2/, "$1");
                    b = a.replace(/\n */g, "\n")
                } catch (e) {
                    b = c
                }
            }
            return b
        },
        uh = function(a, b, c) {
            Pg.call(this, Error(a), {
                message: a,
                file: void 0 === b ? "" : b,
                line: void 0 === c ? -1 : c
            })
        };
    ia(uh, Pg);
    var vh = function() {
        this.h = !1;
        this.g = null
    };
    h = vh.prototype;
    h.qd = function(a) {
        this.g = a
    };
    h.rd = function(a) {
        this.h = a
    };
    h.Ka = function(a, b, c, d, e) {
        if (Math.random() > (void 0 === c ? .01 : c)) return this.h;
        b.error && b.meta && b.id || (b = new Pg(b, {
            context: a,
            id: void 0 === e ? "jserror" : e
        }));
        if (d || this.g) b.meta = {}, this.g && this.g(b.meta), d && d(b.meta);
        n.google_js_errors = n.google_js_errors || [];
        n.google_js_errors.push(b);
        n.error_rep_loaded || (b = n.document, a = b.createElement("script"), Yc(a, Cf(n.location.protocol + "//pagead2.googlesyndication.com/pagead/js/err_rep.js")), (b = b.getElementsByTagName("script")[0]) && b.parentNode && b.parentNode.insertBefore(a,
            b), n.error_rep_loaded = !0);
        return this.h
    };
    h.Ob = function(a, b, c, d) {
        d = void 0 === d ? this.Ka : d;
        try {
            var e = b()
        } catch (f) {
            if (!d.call(this, a, f, .01, c, "jserror")) throw f;
        }
        return e
    };
    h.jd = function(a, b, c, d, e) {
        var f = this;
        e = void 0 === e ? this.Ka : e;
        return function(g) {
            for (var k = [], m = 0; m < arguments.length; ++m) k[m - 0] = arguments[m];
            return f.Ob(a, function() {
                return b.apply(c, k)
            }, d, e)
        }
    };
    var qh, wh, xh = cg(),
        ph = new nh(1, xh);
    qh = new function() {
        var a = void 0 === a ? J : a;
        this.o = "http:" === a.location.protocol ? "http:" : "https:";
        this.h = "pagead2.googlesyndication.com";
        this.l = "/pagead/gen_204?id=";
        this.g = .01
    };
    wh = new rh;
    "complete" == xh.document.readyState ? xh.google_measure_js_timing || ph.v() : ph.h && Wf(xh, "load", function() {
        xh.google_measure_js_timing || ph.v()
    });
    var yh = function(a) {
            wh.qd(function(b) {
                z(a, function(a) {
                    a(b)
                })
            })
        },
        Ah = function(a, b) {
            return wh.Ob(a, b, void 0, zh)
        },
        Bh = function(a, b, c, d) {
            return wh.jd(a, b, c, d, void 0)
        },
        zh = wh.Ka,
        Ch = function(a, b) {
            wh.Ka(a, b, void 0, void 0)
        };
    if (wf && wf.URL) {
        var Dh, Rf = wf.URL;
        Dh = !!Rf && 0 < Sf().length;
        wh.rd(!Dh)
    }
    var Eh = function(a, b, c, d) {
        c = Bh(d, c);
        Wf(a, b, c, {
            capture: !1
        });
        return c
    };
    var Fh = function(a) {
            var b = N.C().A;
            b && (b.h && (a[4] = b.h), b.g && (a[12] = b.g))
        },
        Gh = function(a) {
            var b = [];
            Pa(a, function(a, d) {
                d = encodeURIComponent(d);
                q(a) && (a = encodeURIComponent(a));
                b.push(d + "=" + a)
            });
            b.push("24=" + x());
            return b.join("\n")
        };
    var Hh = !pc && !gc();
    var Ih = function(a) {
        return {
            visible: 1,
            hidden: 2,
            prerender: 3,
            preview: 4,
            unloaded: 5
        }[a.visibilityState || a.webkitVisibilityState || a.mozVisibilityState || ""] || 0
    };

    function Jh(a, b, c, d) {
        if (!a) return {
            value: d,
            done: !1
        };
        d = b(d, a);
        var e = c(d, a);
        return !e && lc(a, "parentElement") ? Jh(od(a), b, c, d) : {
            done: e,
            value: d
        }
    }
    var Kh = function(a, b, c, d) {
        if (!a) return d;
        d = Jh(a, b, c, d);
        if (!d.done) try {
            var e = bd(a),
                f = e && F(e);
            return Kh(f && f.frameElement, b, c, d.value)
        } catch (g) {}
        return d.value
    };

    function Lh(a) {
        var b = !pc || Dc(8);
        return Kh(a, function(a, d) {
            a = lc(d, "style") && d.style && bf(d, "visibility");
            return {
                hidden: "hidden" === a,
                visible: b && "visible" === a
            }
        }, function(a) {
            return a.hidden || a.visible
        }, {
            hidden: !1,
            visible: !1
        }).hidden
    }
    var Mh = function(a) {
            return Kh(a, function(a, c) {
                return !(!lc(c, "style") || !c.style || "none" !== bf(c, "display"))
            }, function(a) {
                return a
            }, !1) ? !0 : Lh(a)
        },
        Nh = function(a) {
            return new I(a.top, a.right, a.bottom, a.left)
        },
        Oh = function(a) {
            return null != a && 0 <= a && 1 >= a
        },
        Ph = function(a, b) {
            b = void 0 === b ? J : b;
            null !== b && b != b.top && (b = b.top);
            var c = 0,
                d = 0;
            try {
                var e = b.document,
                    f = e.body,
                    g = e.documentElement;
                if ("CSS1Compat" == e.compatMode && g.scrollHeight) c = g.scrollHeight != a.height ? g.scrollHeight : g.offsetHeight, d = g.scrollWidth != a.width ? g.scrollWidth :
                    g.offsetWidth;
                else {
                    var k = g.scrollHeight,
                        m = g.scrollWidth,
                        l = g.offsetHeight,
                        t = g.offsetWidth;
                    g.clientHeight != l && (k = f.scrollHeight, m = f.scrollWidth, l = f.offsetHeight, t = f.offsetWidth);
                    k > a.height ? k > l ? (c = k, d = m) : (c = l, d = t) : k < l ? (c = k, d = m) : (c = l, d = t)
                }
                return new E(d, c)
            } catch (H) {
                return new E(-12245933, -12245933)
            }
        };
    var Qh = function(a, b, c, d, e) {
        this.time = a;
        this.h = b;
        this.l = c;
        this.volume = null;
        this.o = d;
        this.g = null;
        this.v = e
    };
    var Rh = function(a, b, c, d, e, f, g, k, m) {
        this.J = a;
        this.B = b;
        this.h = c;
        this.v = d;
        this.A = e;
        this.l = f;
        this.w = g;
        this.H = k;
        this.o = m
    };
    Rh.prototype.g = function() {
        return this.J
    };
    var N = function() {
        this.D = !1;
        this.B = void 0;
        this.h = !Df(J.top);
        var a = Rg();
        a = 0 < a.length && null != a[a.length - 1] && null != a[a.length - 1].url ? ((a = a[a.length - 1].url.match(He)[3] || null) ? decodeURI(a) : a) || "" : "";
        this.domain = a;
        this.v = this.F = this.w = this.l = null;
        this.G = 0;
        this.A = new Ng(0, "");
        this.g = !1;
        this.o = null;
        this.H = 0;
        this.R = "geo";
        this.M = new Ag;
        yg(K(M(this.M, "nio_mode", kg)), pg);
        yg(K(M(this.M, "mv", tg)), ug);
        M(this.M, "omid", jg);
        K(M(this.M, "fcs", jg));
        K(M(this.M, "osd", jg));
        K(M(this.M, "srmi", jg));
        K(M(this.M, "epoh", jg));
        yg(K(M(this.M,
            "umt", jg)), rg);
        yg(K(M(this.M, "gmpd", jg)), sg);
        yg(K(M(this.M, "sel", jg)), qg);
        yg(K(M(this.M, "cll", vg)), wg);
        yg(K(M(this.M, "ioa", jg)), og);
        yg(K(M(this.M, "isu", jg)), mg);
        yg(K(M(this.M, "ald", jg)), ng);
        yg(K(M(this.M, "ftm", jg)), lg);
        K(M(this.M, "inapp", gg));
        this.J = -1
    };
    Ea(N);
    var Sh = function(a) {
        this.h = a;
        this.l = 0;
        this.g = null
    };
    Sh.prototype.cancel = function() {
        J.clearTimeout(this.g);
        this.g = null
    };
    var Th = function(a) {
        J && (a.g = J.setTimeout(Bh(143, function() {
            a.l++;
            a.h.V()
        }), Mg(N.C().B)))
    };
    var Vh = function() {
            return !Uh() && (D("iPod") || D("iPhone") || D("Android") || D("IEMobile"))
        },
        Uh = function() {
            return D("iPad") || D("Android") && !D("Mobile") || D("Silk")
        };
    var Wh = function(a, b, c) {
        this.aa = a;
        this.P = void 0 === c ? "na" : c;
        this.l = [];
        this.J = !1;
        this.o = new Qh(-1, new E(0, 0), new E(0, 0), !0, this);
        this.h = this;
        this.w = this.v = b;
        this.O = Uh() || Vh();
        this.A = !1;
        this.I = null;
        this.D = this.G = !1;
        this.K = "uk";
        this.L = !1
    };
    h = Wh.prototype;
    h.Ya = function() {
        return this.za()
    };
    h.za = function() {
        return !0
    };
    h.uc = function() {
        this.J = !0
    };
    h.fb = function() {
        return this.K
    };
    h.Ua = function() {
        return this.D
    };
    var Yh = function(a, b) {
        a.D || (a.D = !0, a.K = b, a.w = 0, a.B(), a.h == a && (a.v = 0, Xh(a)))
    };
    Wh.prototype.La = function() {
        return this.h == this ? this.P : this.h.La()
    };
    Wh.prototype.Aa = function() {
        return {}
    };
    Wh.prototype.Ba = function() {
        return this.v
    };
    var Zh = function(a, b) {
            rb(a.l, b) || (a.l.push(b), b.Ta(a.h), b.Ja(a.o), b.Ea() && (a.A = !0))
        },
        ai = function(a, b) {
            tb(a.l, b);
            a.A && b.Ea() && $h(a)
        };
    Wh.prototype.V = function() {};
    var $h = function(a) {
        a.A = a.l.length ? nb(a.l, function(a) {
            return a.Ea()
        }) : !1
    };
    Wh.prototype.B = function() {};
    Wh.prototype.g = function() {
        return this.o
    };
    var bi = function(a) {
            var b = xb(a.l);
            z(b, function(b) {
                b.Ja(a.o)
            })
        },
        Xh = function(a) {
            var b = xb(a.l);
            z(b, function(b) {
                b.Ta(a.h)
            });
            a.h != a || bi(a)
        };
    Wh.prototype.Ta = function(a) {
        var b = this.v,
            c = a.Ba();
        this.h = c < this.w ? this : a;
        this.v = this.h != this ? c : this.w;
        this.h == this || 1 == c && 0 != this.w || this.B();
        this.v != b && Xh(this)
    };
    var ci = function(a, b) {
        var c;
        if (!(c = a.G)) {
            c = a.o;
            var d = a.A;
            c = !(b && (void 0 === d || !d || c.volume == b.volume) && c.o == b.o && Ne(c.g, b.g) && $c(c.l, b.l) && $c(c.h, b.h))
        }
        a.o = b;
        c && bi(a)
    };
    Wh.prototype.Ja = function(a) {
        this.h != this && ci(this, a)
    };
    Wh.prototype.Ea = function() {
        return this.A
    };
    Wh.prototype.W = function() {
        this.L = !0
    };
    Wh.prototype.Mb = function() {
        return this.L
    };
    var di = function(a, b, c, d) {
        this.element = a;
        this.o = this.g = b;
        this.M = c;
        this.B = d;
        this.w = !1;
        this.h = new Rh(b.g(), this.element, new I(0, 0, 0, 0), null, this.ub(), 0, 0, O(), 0)
    };
    h = di.prototype;
    h.ed = function() {};
    h.tc = function() {};
    h.sb = function() {
        this.h = new Rh(this.g.g(), this.element, this.h.h, this.h.v, this.ub(), this.h.l, this.h.w, this.h.H, this.h.o)
    };
    h.W = function() {
        this.Mb() || (ai(this.g, this), this.w = !0)
    };
    h.Mb = function() {
        return this.w
    };
    h.Aa = function() {
        return this.o.Aa()
    };
    h.Ba = function() {
        return this.o.Ba()
    };
    h.fb = function() {
        return this.o.fb()
    };
    h.Ua = function() {
        return this.o.Ua()
    };
    h.Ta = function(a) {
        this.o = a;
        this.B.Ta(this)
    };
    h.Ja = function() {
        this.sb()
    };
    h.Ea = function() {
        return this.B.Ea()
    };
    var ei = function(a) {
        this.v = !1;
        this.g = a
    };
    h = ei.prototype;
    h.Ba = function() {
        return this.g.Ba()
    };
    h.fb = function() {
        return this.g.fb()
    };
    h.Ua = function() {
        return this.g.Ua()
    };
    h.create = function(a, b, c) {
        var d = null;
        this.g && (d = this.Jc(a, b, c), Zh(this.g, d));
        return d
    };
    h.Ya = function() {
        return this.za()
    };
    h.za = function() {
        return !1
    };
    h.dd = function() {
        return !0
    };
    h.W = function() {
        this.v = !0
    };
    h.Mb = function() {
        return this.v
    };
    h.Aa = function() {
        return {}
    };
    var fi = function(a, b, c) {
            this.l = void 0 === c ? 0 : c;
            this.h = a;
            this.g = null == b ? "" : b
        },
        gi = function(a, b) {
            return a.l < b.l ? !0 : a.l > b.l ? !1 : a.h < b.h ? !0 : a.h > b.h ? !1 : typeof a.g < typeof b.g ? !0 : typeof a.g > typeof b.g ? !1 : a.g < b.g
        };
    var hi = function() {
        this.l = 0;
        this.g = [];
        this.h = !1
    };
    hi.prototype.add = function(a, b, c) {
        ++this.l;
        a = new fi(a, b, c);
        this.g.push(new fi(a.h, a.g, a.l + this.l / 4096));
        this.h = !0;
        return this
    };
    var ii = function(a) {
            var b = new hi;
            var c = void 0 === c ? 0 : c;
            var d = void 0 === d ? !0 : d;
            Ef(a, function(a, f) {
                d && void 0 === a || b.add(f, a, c)
            });
            return b
        },
        ji = function(a) {
            a.h && (Ab(a.g, function(a, c) {
                return gi(c, a) ? 1 : gi(a, c) ? -1 : 0
            }), a.h = !1);
            return lb(a.g, function(a, c) {
                var b = "boolean" === typeof c.g;
                c = "" + (b && !c.g ? "" : c.h) + (b || "" === c.g ? "" : "=" + c.g);
                return "" + a + ("" != a && "" != c ? "&" : "") + c
            }, "")
        };
    var ki = new Date(0);
    Pb(ki.getUTCFullYear(), 4);
    Pb(ki.getUTCMonth() + 1, 2);
    Pb(ki.getUTCDate(), 2);
    Pb(ki.getUTCHours(), 2);
    Pb(ki.getUTCMinutes(), 2);
    var li = function(a) {
            Ef(a, function(b, c) {
                b instanceof Array && (a[c] = b.join(","))
            });
            return a
        },
        mi = function(a) {
            var b = [],
                c = [];
            Pa(a, function(a, e) {
                if (!(e in Object.prototype) && "undefined" != typeof a) switch (Ga(a) && (a = a.join(",")), a = [e, "=", a].join(""), e) {
                    case "adk":
                    case "r":
                    case "tt":
                    case "error":
                    case "mtos":
                    case "tos":
                    case "p":
                    case "bs":
                    case "aio":
                    case "nio":
                    case "iem":
                        b.unshift(a);
                        break;
                    case "req":
                    case "url":
                    case "referrer":
                    case "iframe_loc":
                        c.push(a);
                        break;
                    default:
                        b.push(a)
                }
            });
            return b.concat(c)
        };
    var ni = {},
        oi = null;
    ni.le = 0;
    ni.nt = 2;
    ni.Fr = 3;
    ni.Po = 5;
    ni.me = 1;
    ni.om = 4;
    var pi = function(a) {
        ni.e = -1;
        ni.i = 6;
        ni.n = 7;
        ni.t = 8;
        if (!oi) {
            var b = [];
            Ef(ni, function(a, c) {
                b[a + 1] = c
            });
            var c = b.join(""),
                d = a && a[c];
            oi = d && function(b, c) {
                return d.call(a, b, c)
            }
        }
        return oi
    };
    var qi = function() {
            this.h = this.l = this.o = this.g = 0
        },
        ri = function(a, b, c, d) {
            b && (a.g += c, a.h += c, a.o += c, a.l = Math.max(a.l, a.o));
            if (void 0 === d ? !b : d) a.o = 0
        };
    var si = [1, .75, .5, .3, 0],
        ti = function(a) {
            this.h = a = void 0 === a ? si : a;
            this.g = kb(this.h, function() {
                return new qi
            })
        },
        vi = function(a, b) {
            return ui(a, function(a) {
                return a.g
            }, void 0 === b ? !0 : b)
        },
        xi = function(a, b) {
            return wi(a, b, function(a) {
                return a.g
            })
        },
        Li = function(a) {
            return ui(a, function(a) {
                return a.l
            }, !0)
        },
        Mi = function(a, b) {
            return wi(a, b, function(a) {
                return a.l
            })
        },
        Ni = function(a, b) {
            return wi(a, b, function(a) {
                return a.h
            })
        },
        Oi = function(a) {
            z(a.g, function(a) {
                a.h = 0
            })
        },
        Pi = function(a, b, c, d, e, f, g) {
            g = void 0 === g ? !0 : g;
            c = f ? Math.min(b,
                c) : c;
            for (f = 0; f < a.h.length; f++) {
                var k = a.h[f],
                    m = 0 < c && c >= k;
                k = !(0 < b && b >= k) || d;
                ri(a.g[f], g && m, e, !g || k)
            }
        },
        ui = function(a, b, c) {
            a = kb(a.g, function(a) {
                return b(a)
            });
            return c ? a : Qi(a)
        },
        wi = function(a, b, c) {
            var d = qb(a.h, function(a) {
                return b <= a
            });
            return -1 == d ? 0 : c(a.g[d])
        },
        Qi = function(a) {
            return kb(a, function(a, c, d) {
                return 0 < c ? d[c] - d[c - 1] : d[c]
            })
        };
    var Ri = function() {
        this.g = new ti;
        this.L = new qi;
        this.G = this.w = -1;
        this.U = 1E3;
        this.V = new ti([1, .9, .8, .7, .6, .5, .4, .3, .2, .1, 0])
    };
    Ri.prototype.D = function(a, b, c, d, e) {
        this.w = -1 != this.w ? Math.min(this.w, b.g) : b.g;
        e && (this.G = Math.max(this.G, b.g));
        Pi(this.V, b.h, c.h, b.l, a, d);
        Pi(this.g, b.g, c.g, b.l, a, d);
        ri(this.L, d || c.Wa != b.Wa ? ig(c) && ig(b) : ig(c), a, !ig(b) || b.l)
    };
    Ri.prototype.Xa = function() {
        return this.L.l >= this.U
    };
    var Si = function(a, b, c, d) {
        di.call(this, a, b, c, d);
        this.l = new I(0, 0, 0, 0)
    };
    ia(Si, di);
    var Ui = function(a, b, c, d) {
            return 0 >= a.h() || 0 >= a.g() ? !0 : c && d ? Ah(208, function() {
                return Ti(a, b, c)
            }) : !1
        },
        Vi = function(a, b) {
            return a.left <= b.right && b.left <= a.right && a.top <= b.bottom && b.top <= a.bottom ? new I(Math.max(a.top, b.top), Math.min(a.right, b.right), Math.min(a.bottom, b.bottom), Math.max(a.left, b.left)) : new I(0, 0, 0, 0)
        },
        Xi = function(a, b) {
            b = Wi(b);
            return 0 === b ? 0 : Wi(a) / b
        },
        Wi = function(a) {
            return Math.max(a.g() * a.h(), 0)
        },
        Ti = function(a, b, c) {
            if (!a || !b) return !1;
            b = Oe(a.clone(), -b.left, -b.top);
            a = (b.left + b.right) / 2;
            b =
                (b.top + b.bottom) / 2;
            var d = cg();
            Df(d.top) && d.top && d.top.document && (d = d.top);
            d = pi(d && d.document);
            if (!d) return !1;
            a = d(a, b);
            if (!a) return !1;
            b = (b = (b = bd(c)) && b.defaultView && b.defaultView.frameElement) && Yi(b, a);
            d = a === c;
            a = !d && a && rd(a, function(a) {
                return a === c
            });
            return !(b || d || a)
        },
        Yi = function(a, b) {
            if (!a || !b) return !1;
            for (var c = 0; null !== a && 100 > c++;) {
                if (a === b) return !0;
                try {
                    if (a = od(a) || a) {
                        var d = bd(a),
                            e = d && F(d),
                            f = e && e.frameElement;
                        f && (a = f)
                    }
                } catch (g) {
                    break
                }
            }
            return !1
        };
    h = Si.prototype;
    h.ub = function() {
        return !0
    };
    h.sb = function() {
        if (this.element) {
            var a = this.element.getBoundingClientRect(),
                b = a.right - a.left;
            a = a.bottom - a.top;
            var c = ef(this.element, this.g.aa),
                d = c.x;
            c = c.y;
            this.l = new I(Math.round(c), Math.round(d + b), Math.round(c + a), Math.round(d))
        }(b = this.g.g().g) ? (b = Vi(this.l, b), b = b.top >= b.bottom || b.left >= b.right ? new I(0, 0, 0, 0) : Oe(b, -this.l.left, -this.l.top)) : b = new I(0, 0, 0, 0);
        var e = this.g.g().g;
        c = d = a = 0;
        var f = 1 == Cg(this.M, "od"),
            g = (this.l.bottom - this.l.top) * (this.l.right - this.l.left);
        e && b && 0 < g && (Ui(b, e, this.element,
            f) ? b = new I(0, 0, 0, 0) : (c = new I(0, window.screen.height, window.screen.width, 0), a = Xi(b, this.l), d = Xi(b, e), c = Xi(b, c)));
        e = this.g.g();
        f = -1 === e.time ? O() : e.time;
        this.h = new Rh(e, this.element, this.l, b, this.ub(), a, d, f, c)
    };
    h.Ua = function() {
        return this.o.Ua()
    };
    h.La = function() {
        return this.o.La()
    };
    h.Ja = function(a) {
        if (null == this.element)
            if (null != a.l) {
                var b = a.l;
                this.l = new I(0, b.width, b.height, 0)
            } else this.l = new I(0, 0, 0, 0);
        di.prototype.Ja.call(this, a)
    };
    var Zi = new I(0, 0, 0, 0),
        $i = {
            threshold: [0, .3, .5, .75, 1]
        },
        aj = function(a, b, c) {
            this.position = Zi.clone();
            this.Fa = 0;
            this.rc = this.Hb();
            this.qc = -2;
            this.bg = x();
            this.ud = -1;
            this.Ab = b;
            this.bb = -1 != b;
            this.Db = this.fd = null;
            this.opacity = -1;
            this.md = c;
            this.vd = this.sc = Da;
            this.Qa = this.element = a;
            this.wd = this.jb = !1;
            this.Vb = 1;
            this.sd = !0;
            this.Ca = !1;
            N.C().G++;
            this.domain = null;
            this.Zc = 0;
            this.Z = this.ac();
            this.td = -1;
            this.Rb = new I(0, 0, 0, 0);
            a = this.M = new Ag;
            M(a, "od", fg);
            K(M(a, "opac", jg));
            M(a, "ud", jg);
            K(M(a, "mkm", jg));
            K(M(a, "xza", jg));
            K(M(a, "mza", jg));
            M(a, "lom", jg);
            K(M(a, "sela", jg));
            K(M(a, "sbeos", jg));
            a = cg().mraid;
            if (b = this.element && this.element.getAttribute) b = this.element, b = /-[a-z]/.test("googleAvInapp") ? !1 : Hh && b.dataset ? "googleAvInapp" in b.dataset : b.hasAttribute ? b.hasAttribute("data-" + Wb()) : !!b.getAttribute("data-" + Wb());
            (b || a) && Bg(N.C().M, "inapp", 1);
            1 == this.md ? Bg(this.M, "od", 1) : Bg(this.M, "od", 0)
        };
    h = aj.prototype;
    h.Ja = function() {};
    h.Ta = function(a) {
        a.Ua() && this.vd(this, a.fb(), a)
    };
    h.Ea = function() {
        return !1
    };
    h.Hb = function() {
        return new Ri
    };
    h.na = function() {
        return this.rc
    };
    var dj = function(a, b, c) {
            if (a.bb) {
                var d = pi(J && J.document);
                d && (c || bj(a, J, !0), d = cj(a, d), a.Za(a.position, d, b, c, !0, !0))
            }
        },
        ej = function(a, b, c) {
            if (c(b)) return b;
            for (;;) {
                var d = Math.floor((a + b) / 2);
                if (d == a || d == b) return a;
                c(d) ? a = d : b = d
            }
        },
        cj = function(a, b) {
            var c = jd(document),
                d = a.Vb,
                e = Math.floor(a.position.left - c.x) + 1,
                f = Math.floor(a.position.top - c.y) + 1,
                g = Math.floor(a.position.right - c.x) - d,
                k = Math.floor(a.position.bottom - c.y) - d;
            a = (k - f) * (g - e);
            if (f > k || e > g) return 0;
            c = !!b(e, f);
            d = !!b(g, k);
            if (c && d) return 1;
            var m = !!b(g, f),
                l = !!b(e, k);
            if (c) k = ej(f, k, function(a) {
                return !!b(e, a)
            }), g = ej(e, g, function(a) {
                return !!b(a, f)
            });
            else if (m) k = ej(f, k, function(a) {
                return !!b(g, a)
            }), e = ej(g, e, function(a) {
                return !!b(a, f)
            });
            else if (l) f = ej(k, f, function(a) {
                return !!b(e, a)
            }), g = ej(e, g, function(a) {
                return !!b(a, k)
            });
            else if (d) f = ej(k, f, function(a) {
                return !!b(g, a)
            }), e = ej(g, e, function(a) {
                return !!b(a, k)
            });
            else {
                var t = Math.floor((e + g) / 2),
                    H = Math.floor((f + k) / 2);
                if (!b(t, H)) return 0;
                f = ej(H, f, function(a) {
                    return !!b(t, a)
                });
                k = ej(H, k, function(a) {
                    return !!b(t, a)
                });
                e =
                    ej(t, e, function(a) {
                        return !!b(a, H)
                    });
                g = ej(t, g, function(a) {
                    return !!b(a, H)
                })
            }
            return (k - f) * (g - e) / a
        },
        fj = function(a, b, c, d, e) {
            a.bb && (d || bj(a, J, e), a.Za(a.position, c, b, d, !1, !0))
        };
    aj.prototype.ad = function() {};
    aj.prototype.$c = function() {};
    aj.prototype.Pc = function() {};
    aj.prototype.Qb = function() {};
    var gj = function(a, b, c) {
            if (a.bb) {
                var d = a.Z.h,
                    e = c ? a.Z.g : a.Zc;
                a.Rb && !Ne(a.Rb, new I(0, 0, 0, 0)) && (e = Oe(a.Rb.clone(), a.position.left, a.position.top));
                a.Za(a.position, e, b, c, !0, !0, {}, void 0, d)
            }
        },
        hj = function(a, b) {
            b = b.create(a.Qa, a.M, a);
            null != b && b.ed();
            b && (a.R = b)
        },
        ij = function(a, b, c) {
            if (a.bb && a.R) {
                var d = cg(),
                    e = N.C();
                bj(a, d, e.h);
                a.R.sb();
                d = a.R.h;
                e = d.g().g;
                var f = !(!d.A && !e);
                if (null != d.v && e) {
                    var g = d.h;
                    a.fd = new Zc(g.left - e.left, g.top - e.top);
                    a.Db = new E(e.right - e.left, e.bottom - e.top)
                }
                a.Za(a.position, d.l, b, c, !0, f, void 0,
                    void 0, d.o)
            }
        };
    h = aj.prototype;
    h.Za = function(a, b, c, d, e, f, g, k, m) {
        g = void 0 === g ? {} : g;
        k = void 0 === k ? this.Oc(c, g) : k;
        g = this.Tb(a, b, d, g, void 0 === m ? -1 : m);
        r(b) || (this.fd = new Zc(a.left - b.left, a.top - b.top), this.Db = new E(b.right - b.left, b.bottom - b.top));
        e = e && this.Z.g >= (this.Wa() ? .3 : .5);
        this.Cc(k, g, e, f);
        this.Ab = c;
        0 < g.g && -1 === this.td && (this.td = c); - 1 == this.ud && this.Xa() && (this.ud = c);
        if (-2 == this.qc) try {
            a: {
                var l = r(b) ? null : b;
                if (a && a != Zi && 0 != this.Fa) {
                    if (!l) {
                        if (!this.Db) {
                            var t = -1;
                            break a
                        }
                        l = new I(0, this.Db.width, this.Db.height, 0)
                    }
                    t = l.h && 0 < l.h() && l.g &&
                        0 < l.g() ? this.tb(a, l) : -1
                } else t = -1
            }
            this.qc = t
        }
        catch (H) {
            Ch(207, H)
        }
        this.Z = g;
        d && (this.Z.g = 0);
        this.sc(this)
    };
    h.Cc = function(a, b, c, d) {
        this.na().D(a, b, this.Z, c, d)
    };
    h.ac = function() {
        return new hg
    };
    h.Tb = function(a, b, c, d, e) {
        e = void 0 === e ? -1 : e;
        d = this.ac();
        d.l = c;
        c = Ih(wf);
        d.o = 0 == c ? -1 : 1 == c ? 0 : 1;
        r(b) ? (d.g = this.tb(b), d.h = e) : (d.g = this.tb(a, b), d.h = 0 <= e ? e : d.g * Wi(a) / (J.screen.height * J.screen.width));
        d.Wa = this.Wa();
        return d
    };
    h.Oc = function(a) {
        if (-1 == this.Ab) return 0;
        a = a - this.Ab || 1;
        return 1E4 < a ? 1 : a
    };
    h.tb = function(a, b) {
        if (0 === this.opacity && 1 === Cg(this.M, "opac")) return 0;
        if (r(a)) return a;
        a = Vi(a, b);
        var c = 1 == Cg(this.M, "od");
        return 0 >= this.Fa || Ui(a, b, this.Qa, c) ? 0 : Wi(a) / this.Fa
    };
    h.Wa = function() {
        return !1
    };
    var bj = function(a, b, c, d) {
        if (d) a.position = d;
        else {
            b = c ? b : b.top;
            try {
                var e = Zi.clone(),
                    f = new Zc(0, 0);
                if (a.Qa) {
                    var g = 1 == a.md;
                    !c && g && Mh(a.Qa) || (e = a.Qa.getBoundingClientRect());
                    f = ef(a.Qa, b)
                }
                c = f;
                var k = c.x,
                    m = c.y;
                a.position = new I(Math.round(m), Math.round(k + (e.right - e.left)), Math.round(m + (e.bottom - e.top)), Math.round(k))
            } catch (l) {
                a.position = Zi.clone()
            }
        }
        a.Fa = (a.position.bottom - a.position.top) * (a.position.right - a.position.left)
    };
    aj.prototype.sa = function() {
        return 0
    };
    aj.prototype.Xa = function() {
        return this.rc.Xa()
    };
    var jj = function(a, b) {
            b = Math.pow(10, b);
            return Math.floor(a * b) / b
        },
        kj = function(a) {
            a.R && a.R.tc()
        },
        mj = function(a, b) {
            var c = !1,
                d = a.Qa;
            if (null === d) return !1;
            Ah(152, function() {
                var e = new b.IntersectionObserver(function(c) {
                    try {
                        lj(b, c, a)
                    } catch (g) {
                        try {
                            e.unobserve(d), Ch("osd_adblock::nioc", g)
                        } catch (k) {}
                    }
                }, $i);
                e.observe(d);
                c = !0
            });
            return c
        },
        nj = function(a, b) {
            var c = !1;
            Ah(151, function() {
                var d = $f(b).observeIntersection(function(c) {
                    try {
                        lj(b, c, a)
                    } catch (f) {
                        try {
                            d(), Ch("osd_adblock::aioc", f)
                        } catch (g) {}
                    }
                });
                c = !0
            });
            return c
        },
        lj = function(a, b, c) {
            if (!b || !b.length || 0 >= b.length) b = null;
            else {
                for (var d = b[0], e = 1; e < b.length; e++) b[e].time > d.time && (d = b[e]);
                b = d
            }
            if (e = b) {
                d = e.intersectionRect.width * e.intersectionRect.height / (e.boundingClientRect.width * e.boundingClientRect.height);
                b = Nh(e.boundingClientRect);
                e = Nh(e.intersectionRect);
                var f = d * Wi(b) / (a.screen.height * a.screen.width);
                c.Z.g = Math.min(Math.max(d, 0), 1);
                c.Zc = c.Z.g;
                c.Z.h = Math.min(Math.max(f, 0), 1);
                bj(c, a, !0, b);
                a = Vi(b, e);
                c.Rb = 0 >= c.Fa || a.top >= a.bottom || a.left >= a.right ? new I(0, 0, 0, 0) :
                    Oe(a, -b.left, -b.top)
            }
        },
        oj = function(a, b, c, d) {
            if (d = void 0 === d ? Da : d) a.vd = d;
            switch (c) {
                case "nio":
                    return mj(a, b);
                case "aio":
                    return nj(a, b);
                case "geo":
                case "iem":
                    return !0
            }
            return !1
        };
    var pj = "StopIteration" in n ? n.StopIteration : {
            message: "StopIteration",
            stack: ""
        },
        qj = function() {};
    qj.prototype.next = function() {
        throw pj;
    };
    qj.prototype.qb = function() {
        return this
    };
    var rj = function(a) {
            if (a instanceof qj) return a;
            if ("function" == typeof a.qb) return a.qb(!1);
            if (Ha(a)) {
                var b = 0,
                    c = new qj;
                c.next = function() {
                    for (;;) {
                        if (b >= a.length) throw pj;
                        if (b in a) return a[b++];
                        b++
                    }
                };
                return c
            }
            throw Error("Not implemented");
        },
        sj = function(a, b, c) {
            if (Ha(a)) try {
                z(a, b, c)
            } catch (d) {
                if (d !== pj) throw d;
            } else {
                a = rj(a);
                try {
                    for (;;) b.call(c, a.next(), void 0, a)
                } catch (d) {
                    if (d !== pj) throw d;
                }
            }
        };
    var tj = function(a) {
        for (var b = 0, c = a, d = 0; a && a != a.parent;) a = a.parent, d++, Df(a) && (c = a, b = d);
        return {
            aa: c,
            level: b
        }
    };
    var uj = function(a) {
        dh.call(this, a);
        this.dfltBktExt = this.h;
        this.lrsExt = this.g
    };
    ia(uj, dh);
    var vj = function() {
            this.S = {}
        },
        xj = function() {
            if (wj) return wj;
            var a = $f();
            a = (a ? Df(a.master) ? a.master : null : null) || cg();
            var b = a.google_persistent_state_async;
            return null != b && "object" == typeof b && null != b.S && "object" == typeof b.S ? wj = b : a.google_persistent_state_async = wj = new vj
        },
        zj = function(a, b, c) {
            b = yj[b] || "google_ps_" + b;
            a = a.S;
            var d = a[b];
            return void 0 === d ? a[b] = c : d
        },
        Aj = function() {
            var a = xj();
            var b = cg();
            var c = $f(b);
            c ? ((c = c || $f()) ? (b = c.pageViewId, c = c.clientId, q(c) && (b += c.replace(/\D/g, "").substr(0, 6))) : b = null, b = +b) : (b = tj(b).aa, (c = b.google_global_correlator) || (b.google_global_correlator = c = 1 + Math.floor(Math.random() * Math.pow(2, 43))), b = c);
            return zj(a, 7, b)
        },
        wj = null,
        Bj = {},
        yj = (Bj[8] = "google_prev_ad_formats_by_region", Bj[9] = "google_prev_ad_slotnames_by_region", Bj);
    var Ff = {
            Zg: 5,
            Pg: 7,
            gh: 17,
            Eg: 19,
            Ag: 41,
            Ig: 48,
            Ph: 62,
            Ah: 67,
            Nh: 69,
            ri: 74,
            ki: 79,
            Qh: 82,
            Rh: 83,
            Jh: 87,
            Vh: 88,
            Cg: 89,
            Kh: 90,
            Fg: 103,
            qg: 104,
            $g: 106,
            si: 107,
            Sg: 108,
            Bh: 114,
            Mh: 118,
            rg: 119,
            yh: 121,
            zh: 122,
            xh: 123,
            gg: 124,
            bi: 125,
            oi: 126,
            pg: 127
        },
        Cj = null,
        Dj = function(a) {
            try {
                return !!a && kc(!0)
            } catch (b) {
                return !1
            }
        },
        Ej = function() {
            if (Dj(Cj)) return !0;
            var a = xj();
            if (a = zj(a, 3, null)) {
                if (a && a.dfltBktExt && a.lrsExt) {
                    var b = new uj;
                    b.h = a.dfltBktExt;
                    b.dfltBktExt = b.h;
                    b.g = a.lrsExt;
                    b.lrsExt = b.g;
                    a = b
                } else a = null;
                a || (a = new uj, b = {
                        context: "ps::gpes::cf",
                        url: cg().location.href
                    },
                    bh(qh, "jserror", b, void 0, void 0))
            }
            return Dj(a) ? (Cj = a, !0) : !1
        },
        eh = function() {
            if (Ej()) return Cj;
            var a = xj(),
                b = new uj(Gf());
            return Cj = a.S[yj[3] || "google_ps_3"] = b
        },
        Fj = null;
    var Gj = {
            currentTime: 1,
            duration: 2,
            isVpaid: 4,
            volume: 8,
            isYouTube: 16,
            isPlaying: 32
        },
        Za = {
            Fc: "start",
            FIRST_QUARTILE: "firstquartile",
            MIDPOINT: "midpoint",
            THIRD_QUARTILE: "thirdquartile",
            COMPLETE: "complete",
            Hd: "metric",
            Ec: "pause",
            Jd: "resume",
            SKIPPED: "skip",
            VIEWABLE_IMPRESSION: "viewable_impression",
            Id: "mute",
            Kd: "unmute",
            FULLSCREEN: "fullscreen",
            Ed: "exitfullscreen",
            yg: "bufferstart",
            xg: "bufferfinish",
            Fd: "fully_viewable_audible_half_duration_impression",
            Gd: "measurable_impression",
            xd: "abandon",
            Dd: "engagedview",
            IMPRESSION: "impression",
            Bd: "creativeview",
            LOADED: "loaded",
            Oh: "progress",
            zg: "close",
            Bg: "collapse",
            Eh: "overlay_resize",
            Fh: "overlay_unmeasurable_impression",
            Gh: "overlay_unviewable_impression",
            Ih: "overlay_viewable_immediate_impression",
            Hh: "overlay_viewable_end_of_session_impression",
            Cd: "custom_metric_viewable"
        },
        Hj = "start firstquartile midpoint thirdquartile resume loaded".split(" "),
        Ij = ["start", "firstquartile", "midpoint", "thirdquartile"],
        Jj = ["abandon"],
        Kj = {
            mi: -1,
            Fc: 0,
            FIRST_QUARTILE: 1,
            MIDPOINT: 2,
            THIRD_QUARTILE: 3,
            COMPLETE: 4,
            Hd: 5,
            Ec: 6,
            Jd: 7,
            SKIPPED: 8,
            VIEWABLE_IMPRESSION: 9,
            Id: 10,
            Kd: 11,
            FULLSCREEN: 12,
            Ed: 13,
            Fd: 14,
            Gd: 15,
            xd: 16,
            Dd: 17,
            IMPRESSION: 18,
            Bd: 19,
            LOADED: 20,
            Cd: 21
        };
    var Lj = function() {
            this.o = this.g = this.l = this.h = this.v = 0
        },
        Mj = function(a) {
            var b = {};
            var c = x() - a.v;
            b = (b.ptlt = c, b);
            (c = a.h) && (b.pnk = c);
            (c = a.l) && (b.pnc = c);
            (c = a.o) && (b.pnmm = c);
            (a = a.g) && (b.pns = a);
            return b
        };
    var Nj = function() {
        hg.call(this);
        this.v = !1;
        this.volume = void 0;
        this.w = !1;
        this.A = -1
    };
    ia(Nj, hg);
    var Oj = function(a) {
        return Oh(a.volume) && .1 <= a.volume
    };
    var Pj = function() {
            var a = {};
            this.h = (a.vs = [1, 0], a.vw = [0, 1], a.am = [2, 2], a.a = [4, 4], a.f = [8, 8], a.bm = [16, 16], a.b = [32, 32], a.avw = [0, 64], a.cm = [128, 128], a.pv = [256, 256], a.gdr = [0, 512], a.p = [0, 1024], a.r = [0, 2048], a.m = [0, 4096], a.um = [0, 8192], a.ef = [0, 16384], a.s = [0, 32768], a.pmx = [0, 16777216], a);
            this.g = {};
            for (var b in this.h) 0 < this.h[b][1] && (this.g[b] = 0);
            this.l = 0
        },
        Qj = function(a, b) {
            var c = a.h[b],
                d = c[1];
            a.l += c[0];
            0 < d && 0 == a.g[b] && (a.g[b] = 1)
        },
        Sj = function(a) {
            return Rj(a, Va(a.h))
        },
        Rj = function(a, b) {
            var c = 0,
                d;
            for (d in a.g) rb(b,
                d) && 1 == a.g[d] && (c += a.h[d][1], a.g[d] = 2);
            return c
        },
        Tj = function(a) {
            var b = 0,
                c;
            for (c in a.g) {
                var d = a.g[c];
                if (1 == d || 2 == d) b += a.h[c][1]
            }
            return b
        };
    var Uj = function() {
        this.h = this.l = 0
    };
    Uj.prototype.g = function() {
        return this.l
    };
    var Vj = function(a, b, c) {
        32 <= b || (a.h & 1 << b && !c ? a.l &= ~(1 << b) : a.h & 1 << b || !c || (a.l |= 1 << b), a.h |= 1 << b)
    };
    var Wj = function() {
        Ri.call(this);
        this.l = new qi;
        this.K = this.H = this.F = 0;
        this.B = -1;
        this.X = new qi;
        this.v = new qi;
        this.h = new ti;
        this.A = this.o = -1;
        this.J = new qi;
        this.U = 2E3;
        this.I = new Uj;
        this.P = new Uj;
        this.O = new Uj
    };
    ia(Wj, Ri);
    var Xj = function(a, b, c) {
        var d = a.K;
        Lg || c || -1 == a.B || (d += b - a.B);
        return d
    };
    Wj.prototype.D = function(a, b, c, d, e) {
        if (!b.w) {
            Ri.prototype.D.call(this, a, b, c, d, e);
            e = Oj(b) && Oj(c);
            var f = .5 <= (d ? Math.min(b.g, c.g) : c.g);
            Oh(b.volume) && (this.o = -1 != this.o ? Math.min(this.o, b.volume) : b.volume, this.A = Math.max(this.A, b.volume));
            f && (this.F += a, this.H += e ? a : 0);
            Pi(this.h, b.g, c.g, b.l, a, d, e);
            ri(this.l, !0, a);
            ri(this.v, e, a);
            ri(this.J, c.v, a);
            ri(this.X, e && !f, a);
            a = Math.floor(b.A / 1E3);
            Vj(this.I, a, ig(b));
            Vj(this.P, a, 1 <= b.g);
            Vj(this.O, a, Oj(b))
        }
    };
    var Yj = function() {
        this.g = !1
    };
    var Zj = function(a, b) {
        this.g = !1;
        this.o = a;
        this.H = b;
        this.h = 0
    };
    ia(Zj, Yj);
    var bk = function(a, b) {
        return a.l(b) ? (b = ak(a.H, a.o, b), a.h |= b, 0 == b) : !1
    };
    Zj.prototype.l = function() {
        return !0
    };
    Zj.prototype.v = function() {
        return !1
    };
    Zj.prototype.A = function() {
        var a = this,
            b = $a(function(b) {
                return b == a.o
            });
        return Kj[b].toString()
    };
    Zj.prototype.toString = function() {
        var a = "";
        this.v() && (a += "c");
        this.g && (a += "s");
        0 < this.h && (a += ":" + this.h);
        return this.A() + a
    };
    var ck = new I(0, 0, 0, 0),
        dk = {},
        ek = (dk.firstquartile = 0, dk.midpoint = 1, dk.thirdquartile = 2, dk.complete = 3, dk),
        fk = function(a, b, c, d, e, f) {
            e = void 0 === e ? null : e;
            f = void 0 === f ? [] : f;
            aj.call(this, b, c, d);
            this.O = 0;
            this.l = {};
            this.ba = new Pj;
            this.bd = {};
            this.ia = "";
            this.Ma = null;
            this.Pa = !1;
            this.h = [];
            this.B = e;
            this.w = f;
            this.o = void 0;
            this.v = -1;
            this.X = this.J = void 0;
            this.P = !1;
            this.F = this.D = 0;
            this.K = -1;
            this.V = this.ma = !1;
            this.pb = this.ya = 0;
            this.ea = !1;
            this.ra = this.wa = -1;
            this.I = this.H = this.g = 0;
            this.Yb = this.Ub = -1;
            this.Xb = 0;
            this.ab = new ti;
            this.L = this.fa = this.Gb = 0;
            this.Oa = -1;
            this.ka = 0;
            this.pa = !1;
            this.U = null;
            this.$ = 0;
            this.ca = Da;
            this.G = [this.Hb()];
            this.wd = !0;
            this.Vb = 2;
            b = N.C();
            bj(this, a, b.h);
            this.$a = {};
            this.$a.pause = "p";
            this.$a.resume = "r";
            this.$a.skip = "s";
            this.$a.mute = "m";
            this.$a.unmute = "um";
            this.$a.exitfullscreen = "ef";
            this.A = null
        };
    ia(fk, aj);
    fk.prototype.Ea = function() {
        return !0
    };
    var gk = function(a, b, c) {
        a.$ = 1;
        a.l = {};
        a.l.firstquartile = !1;
        a.l.midpoint = !1;
        a.l.thirdquartile = !1;
        a.l.complete = !1;
        a.l.pause = !1;
        a.l.skip = !1;
        a.l.viewable_impression = !1;
        a.O = 0;
        c || (a.na().B = b)
    };
    fk.prototype.Bc = function() {
        if (this.B) {
            var a = this.B;
            a.g || (a.g = bk(a, this))
        }
    };
    fk.prototype.ad = function(a) {
        var b = this,
            c = a - this.wa;
        this.ea && 1E3 >= c || (c = Ca("ima.bridge.getNativeViewability"), v(c) && (c(this.ia, function(a) {
            b.ea = !1;
            ab(a) && b.ka++;
            b.Qb(a)
        }), this.ea = !0, this.wa = a))
    };
    fk.prototype.$c = function(a) {
        var b = N.C();
        a - this.ra > Mg(b.B) && (a = Ca("ima.admob.getViewability"), v(a) && a(this.ia))
    };
    var hk = function(a) {
        return p(a) ? Number(a) ? jj(a, 3) : 0 : a
    };
    h = fk.prototype;
    h.Pc = function(a) {
        this.ra = O();
        this.Qb(a)
    };
    h.Qb = function(a) {
        var b = a.opt_nativeViewBounds || {},
            c = a.opt_nativeViewVisibleBounds || {},
            d = a.opt_nativeTime || -1,
            e = a.opt_nativeVolume,
            f = a.opt_nativeViewAttached;
        a = a.opt_nativeViewHidden;
        void 0 !== f && (this.U = !!f);
        b = new I(b.top || 0, b.left + b.width || 0, b.top + b.height || 0, b.left || 0);
        c = a ? ck.clone() : new I(c.top || 0, c.left + c.width || 0, c.top + c.height || 0, c.left || 0);
        f = void 0;
        if ("n" == this.o || "ml" == this.o) f = {
            volume: e
        };
        e = f;
        e = void 0 === e ? {} : e;
        this.Fa = (b.bottom - b.top) * (b.right - b.left);
        this.position = b;
        this.Za(b, c, d, !1, !0, !0,
            e)
    };
    h.Za = function(a, b, c, d, e, f, g, k, m) {
        var l = this;
        g = void 0 === g ? {} : g;
        var t = this.ca(this) || {};
        eb(t, g);
        this.v = t.duration || this.v;
        this.J = t.isVpaid || this.J;
        this.X = t.isYouTube || this.X;
        this.P = f;
        aj.prototype.Za.call(this, a, b, c, d, e, f, t, k, m);
        z(this.w, function(a) {
            a.g || (a.g = bk(a, l))
        })
    };
    h.Cc = function(a, b, c, d) {
        aj.prototype.Cc.call(this, a, b, c, d);
        ik(this).D(a, b, this.Z, c, d);
        this.V = Oj(this.Z) && Oj(b); - 1 == this.K && this.ma && (this.K = this.na().l.g);
        this.ba.l = 0;
        a = this.Z;
        b = this.Xa();
        .5 <= a.g && Qj(this.ba, "vs");
        b && Qj(this.ba, "vw");
        Oh(a.volume) && Qj(this.ba, "am");
        this.V && Qj(this.ba, "a");
        this.Ca && Qj(this.ba, "f"); - 1 != a.o && (Qj(this.ba, "bm"), 1 == a.o && Qj(this.ba, "b"));
        this.V && b && Qj(this.ba, "avw");
        this.P && Qj(this.ba, "cm");
        this.P && 0 < a.g && Qj(this.ba, "pv");
        jk(this, this.na().l.g, !0) && Qj(this.ba, "gdr");
        2E3 <=
            Mi(this.na().g, 1) && Qj(this.ba, "pmx")
    };
    h.Hb = function() {
        return new Wj
    };
    h.na = function() {
        return this.rc
    };
    var ik = function(a, b) {
        var c;
        null != b && b < a.G.length ? c = b : c = a.G.length - 1;
        return a.G[c]
    };
    fk.prototype.ac = function() {
        return new Nj
    };
    fk.prototype.Tb = function(a, b, c, d, e) {
        a = aj.prototype.Tb.call(this, a, b, c, d, void 0 === e ? -1 : e);
        a.v = this.Ca;
        a.w = 2 == this.$;
        a.volume = d.volume;
        Oh(a.volume) || (this.ya++, b = this.Z, Oh(b.volume) && (a.volume = b.volume));
        d = d.currentTime;
        a.A = p(d) && 0 <= d ? d : -1;
        return a
    };
    var kk = function(a) {
        var b = !!Cg(N.C().M, "umt");
        return a.J || !b && !a.X ? 0 : 1
    };
    fk.prototype.Oc = function(a, b) {
        b = p(b.currentTime) ? b.currentTime : this.D;
        if (-1 == this.Ab || 2 == this.$) a = 0;
        else {
            a = a - this.Ab || 1;
            var c = 1E4;
            p(this.v) && -1 != this.v && (c = Math.max(c, this.v / 3));
            a = a > c ? 1 : a
        }
        c = b - this.D;
        var d = 0;
        0 <= c ? (this.F += a, this.L += Math.max(a - c, 0), d = Math.min(c, this.F)) : this.fa += Math.abs(c);
        0 != c && (this.F = 0); - 1 == this.Oa && 0 < c && (this.Oa = 0 <= Kg ? O() - Kg : -1);
        this.D = b;
        return 1 == kk(this) ? d : a
    };
    fk.prototype.tb = function(a, b) {
        return this.pa ? 0 : this.Ca ? 1 : aj.prototype.tb.call(this, a, b)
    };
    fk.prototype.sa = function() {
        return 1
    };
    var lk = function(a, b) {
            nb(a.w, function(a) {
                return a.o == b.o
            }) || a.w.push(b)
        },
        jk = function(a, b, c) {
            return 15E3 <= b ? !0 : a.ma ? (void 0 === c ? 0 : c) ? !0 : mk(a.v) ? b >= a.v / 2 : mk(a.K) ? b >= a.K : !1 : !1
        },
        mk = function(a) {
            return 1 == Cg(N.C().M, "gmpd") ? 0 < a : -1 != a
        },
        nk = function(a) {
            var b = {},
                c = N.C();
            b.insideIframe = c.h;
            b.unmeasurable = a.jb;
            b.position = a.position;
            b.exposure = a.Z.g;
            b.documentSize = c.v;
            b.viewportSize = c.l;
            null != a.A && (b.presenceData = a.A);
            b.screenShare = a.Z.h;
            return b
        },
        pk = function(a, b) {
            ok(a.h, b, function() {
                return {
                    fg: 0,
                    Lb: void 0
                }
            });
            a.h[b] = {
                viewableArea: jj(a.Z.g, 2),
                instantaneousState: a.ba.l
            }
        },
        ok = function(a, b, c) {
            for (var d = a.length; d < b + 1;) a.push(c()), d++
        },
        sk = function(a, b, c) {
            var d = a.bd[b];
            if (null != d) return d;
            d = qk(a, b);
            var e = $a(function(a) {
                return a == b
            });
            c = rk(a, d, d, c, ek[Za[e]]);
            "fully_viewable_audible_half_duration_impression" == b && (c.std = "csm", c.ic = Rj(a.ba, ["gdr"]));
            return c
        },
        rk = function(a, b, c, d, e) {
            if (a.jb) return {
                "if": 0
            };
            var f = a.position.clone();
            f.round();
            var g = kb(a.h, function(a) {
                    return 100 * a.fg | 0
                }),
                k = N.C(),
                m = a.na(),
                l = {};
            l["if"] = k.h ? 1 : void 0;
            l.sdk = a.o ? a.o : void 0;
            l.t = a.bg;
            l.p = [f.top, f.left, f.bottom, f.right];
            l.tos = vi(m.g, !1);
            l.mtos = Li(m.g);
            l.mcvt = m.L.l;
            l.ps = void 0;
            l.pt = g;
            f = Xj(m, O(), 2 == a.$);
            l.vht = f;
            l.mut = m.X.l;
            l.a = hk(a.Z.volume);
            l.mv = hk(m.A);
            l.fs = a.Ca ? 1 : 0;
            l.ft = m.J.g;
            l.at = m.v.g;
            l.as = .1 <= m.o ? 1 : 0;
            l.atos = vi(m.h);
            l.ssb = vi(m.V, !1);
            l.amtos = Li(m.h);
            l.uac = a.ya;
            l.vpt = m.l.g;
            "nio" == k.R && (l.nio = 1, l.avms = "nio");
            l.gmm = "4";
            l.gdr = jk(a, m.l.g, !0) ? 1 : 0;
            a.wd && (l.efpf = a.Vb);
            0 < a.ka && (l.nnut = a.ka);
            l.tcm = kk(a);
            l.nmt = a.fa;
            l.bt = a.L;
            l.pst = a.Oa;
            l.vpaid = a.J;
            l.dur = a.v;
            l.vmtime = a.D;
            l.is = a.ba.l;
            1 <= a.h.length && (l.i0 = a.h[0].Lb);
            2 <= a.h.length && (l.i1 = a.h[1].Lb);
            3 <= a.h.length && (l.i2 = a.h[2].Lb);
            4 <= a.h.length && (l.i3 = a.h[3].Lb);
            l.cs = Tj(a.ba);
            b && (l.ic = Sj(a.ba), l.dvpt = m.l.h, l.dvs = Ni(m.g, .5), l.dfvs = Ni(m.g, 1), l.davs = Ni(m.h, .5), l.dafvs = Ni(m.h, 1), c && (m.l.h = 0, Oi(m.g), Oi(m.h)), a.Xa() && (l.dtos = m.F, l.dav = m.H, l.dtoss = a.O + 1, c && (m.F = 0, m.H = 0, a.O++)), l.dat = m.v.h, l.dft = m.J.h, c && (m.v.h = 0, m.J.h = 0));
            k.v && (l.ps = [k.v.width, k.v.height]);
            k.l && (l.bs = [k.l.width, k.l.height]);
            k.w && (l.scs = [k.w.width,
                k.w.height
            ]);
            l.dom = k.domain;
            a.pb && (l.vds = a.pb);
            if (0 < a.w.length || a.B) b = xb(a.w), a.B && b.push(a.B), l.pings = kb(b, function(a) {
                return a.toString()
            });
            b = kb(jb(a.w, function(a) {
                return a.v()
            }), function(a) {
                return a.A()
            });
            yb(b);
            l.ces = b;
            a.g && (l.vmer = a.g);
            a.H && (l.vmmk = a.H);
            a.I && (l.vmiec = a.I);
            l.avms = a.R ? a.R.La() : N.C().R;
            a.R && eb(l, a.R.Aa());
            "exc" == k.R && (l.femt = a.Ub, l.femvt = a.Yb, l.emc = a.Xb, l.emb = vi(a.ab, !1), l.emuc = a.Gb, l.avms = "exc");
            d ? (l.c = jj(a.Z.g, 2), l.ss = jj(a.Z.h, 2)) : l.tth = O() - Jg;
            l.mc = jj(m.G, 2);
            l.nc = jj(m.w, 2);
            l.mv =
                hk(m.A);
            l.nv = hk(m.o);
            l.lte = jj(a.qc, 2);
            d = ik(a, e);
            Li(m.g);
            l.qmtos = Li(d.g);
            l.qnc = jj(d.w, 2);
            l.qmv = hk(d.A);
            l.qnv = hk(d.o);
            l.qas = .1 <= d.o ? 1 : 0;
            l.qi = a.ia;
            null !== a.U && (l.nvat = a.U ? 1 : 0);
            l.avms || (l.avms = "geo");
            l.psm = m.I.h;
            l.psv = m.I.g();
            l.psfv = m.P.g();
            l.psa = m.O.g();
            k = Eg(k.M);
            k.length && (l.veid = k);
            a.A && eb(l, Mj(a.A));
            return l
        },
        qk = function(a, b) {
            if (rb(Jj, b)) return !0;
            var c = a.l[b];
            return p(c) ? (a.l[b] = !0, !c) : !1
        };
    var tk = x(),
        wk = function() {
            this.g = {};
            var a = F();
            uk(this, a, document);
            var b = vk();
            try {
                if ("1" == b) {
                    for (var c = a.parent; c != a.top; c = c.parent) uk(this, c, c.document);
                    uk(this, a.top, a.top.document)
                }
            } catch (d) {}
        },
        vk = function() {
            var a = document.documentElement;
            try {
                if (!Df(F().top)) return "2";
                var b = [],
                    c = F(a.ownerDocument);
                for (a = c; a != c.top; a = a.parent)
                    if (a.frameElement) b.push(a.frameElement);
                    else break;
                return b && 0 != b.length ? "1" : "0"
            } catch (d) {
                return "2"
            }
        },
        uk = function(a, b, c) {
            Eh(c, "mousedown", function() {
                return xk(a)
            }, 301);
            Eh(b,
                "scroll",
                function() {
                    return yk(a)
                }, 302);
            Eh(c, "touchmove", function() {
                return zk(a)
            }, 303);
            Eh(c, "mousemove", function() {
                return Ak(a)
            }, 304);
            Eh(c, "keydown", function() {
                return Bk(a)
            }, 305)
        },
        xk = function(a) {
            Pa(a.g, function(a) {
                1E5 < a.l || ++a.l
            })
        },
        yk = function(a) {
            Pa(a.g, function(a) {
                1E5 < a.g || ++a.g
            })
        },
        zk = function(a) {
            Pa(a.g, function(a) {
                1E5 < a.g || ++a.g
            })
        },
        Bk = function(a) {
            Pa(a.g, function(a) {
                1E5 < a.h || ++a.h
            })
        },
        Ak = function(a) {
            Pa(a.g, function(a) {
                1E5 < a.o || ++a.o
            })
        };
    var Ck = function() {
            this.g = this.h = null
        },
        Dk = function(a, b) {
            if (null == a.h) return !1;
            var c = function(c, e) {
                a.g = null;
                b(c, e)
            };
            a.g = pb(a.h, function(a) {
                return null != a && a.Ya() && a.dd(c)
            });
            return null != a.g
        };
    Ea(Ck);
    var Ek = {
            threshold: [0, .25, .5, .75, 1]
        },
        Fk = function(a, b, c, d) {
            di.call(this, a, b, c, d);
            this.A = this.v = this.l = null
        };
    ia(Fk, di);
    Fk.prototype.La = function() {
        return "nio"
    };
    Fk.prototype.ub = function() {
        return !0
    };
    Fk.prototype.ed = function() {
        var a = this;
        this.A || (this.A = O());
        Ah(298, function() {
            return Gk(a)
        }) || Yh(this.g, "msf")
    };
    Fk.prototype.tc = function() {
        if (this.l && this.element) try {
            this.l.unobserve(this.element)
        } catch (a) {}
    };
    var Gk = function(a) {
        if (!a.element) return !1;
        var b = a.element;
        a.l = new a.g.aa.IntersectionObserver(function(b) {
            return Hk(a, b)
        }, Ek);
        a.l.observe(b);
        2 === Cg(N.C().M, "nio_mode") && Hk(a, a.l && a.l.takeRecords ? a.l.takeRecords() : []);
        return !0
    };
    Fk.prototype.sb = function() {
        di.prototype.sb.call(this);
        if (2 === Cg(N.C().M, "nio_mode")) {
            var a = this.l && this.l.takeRecords ? this.l.takeRecords() : [];
            0 < a.length ? Hk(this, a) : this.h = new Rh(this.h.g(), this.h.B, this.h.h, this.h.v, this.h.A, this.h.l, this.h.w, this.g.aa.performance.now(), this.h.o)
        }
    };
    var Hk = function(a, b) {
            try {
                if (b.length) {
                    a.v || (a.v = O());
                    var c = Ik(b),
                        d = Nh(c.boundingClientRect),
                        e = Oe(Nh(c.intersectionRect), -d.left, -d.top),
                        f = O(),
                        g = c.boundingClientRect.width * c.boundingClientRect.height,
                        k = c.intersectionRect.width * c.intersectionRect.height;
                    var m = g ? k / g : 0;
                    b = 0;
                    var l = c.intersectionRect.width * c.intersectionRect.height,
                        t = a.g.g().g;
                    t && (b = (t.bottom - t.top) * (t.right - t.left));
                    var H = c.intersectionRect.width * c.intersectionRect.height,
                        fa = window.screen.height * window.screen.width;
                    a.h = new Rh(a.g.g(), a.element,
                        d, e, a.ub(), m, b ? l / b : 0, f, H && fa ? H / fa : 0)
                }
            } catch (va) {
                a.tc(), Ch(299, va)
            }
        },
        Ik = function(a) {
            return lb(a, function(a, c) {
                return a.time > c.time ? a : c
            }, a[0])
        };
    Fk.prototype.Aa = function() {
        var a = {};
        return Object.assign(this.g.Aa(), (a.niot_obs = this.A, a.niot_cbk = this.v, a))
    };
    var Jk = function(a) {
        a = void 0 === a ? J : a;
        ei.call(this, new Wh(a, 2))
    };
    ia(Jk, ei);
    Jk.prototype.La = function() {
        return "nio"
    };
    Jk.prototype.Ya = function() {
        var a = Cg(N.C().M, "nio_mode"),
            b = 2 === a;
        a = 1 === a;
        var c = N.C().h;
        return (b || a && c) && this.za()
    };
    Jk.prototype.za = function() {
        return "exc" !== N.C().R && 1 != Cg(N.C().M, "inapp") && null != this.g.aa.IntersectionObserver
    };
    Jk.prototype.Jc = function(a, b, c) {
        return new Fk(a, this.g, b, c)
    };
    var Kk = function(a, b, c) {
        wd.call(this);
        this.o = null != c ? w(a, c) : a;
        this.l = b;
        this.h = w(this.Tf, this);
        this.g = []
    };
    y(Kk, wd);
    h = Kk.prototype;
    h.nb = !1;
    h.Cb = 0;
    h.Sa = null;
    h.Lc = function(a) {
        this.g = arguments;
        this.Sa || this.Cb ? this.nb = !0 : Lk(this)
    };
    h.stop = function() {
        this.Sa && (n.clearTimeout(this.Sa), this.Sa = null, this.nb = !1, this.g = [])
    };
    h.pause = function() {
        this.Cb++
    };
    h.resume = function() {
        this.Cb--;
        this.Cb || !this.nb || this.Sa || (this.nb = !1, Lk(this))
    };
    h.T = function() {
        Kk.da.T.call(this);
        this.stop()
    };
    h.Tf = function() {
        this.Sa = null;
        this.nb && !this.Cb && (this.nb = !1, Lk(this))
    };
    var Lk = function(a) {
        a.Sa = fe(a.h, a.l);
        a.o.apply(null, a.g)
    };
    var Mk = function() {
            this.g = this.h = null
        },
        Nk = function() {
            this.g = [];
            this.h = [];
            this.done = !1;
            this.l = {
                wi: 0,
                Md: 0,
                pd: 0,
                Qd: 0,
                Gf: -1
            };
            this.D = this.v = this.B = this.A = this.H = null;
            this.G = !1;
            this.I = null;
            this.F = Uh() || Vh();
            this.o = new Sh(this)
        },
        Ok = function() {
            var a = N.C().R;
            return "nio" == a || "aio" == a
        },
        Qk = function() {
            var a = P;
            a.G || (a.G = !0, a.H || Ok() || (a.A = new Kk(Bh(137, function(b) {
                for (var c = [], d = 0; d < arguments.length; ++d) c[d - 0] = arguments[d];
                return a.w.apply(a, sa(c))
            }), 100), a.H = Eh(J, "scroll", function(b) {
                for (var c = [], d = 0; d < arguments.length; ++d) c[d -
                    0] = arguments[d];
                null !== a.A && a.A.Lc.apply(a.A, sa(c))
            }, 138)), a.B || Ok() || (a.v = new Kk(Bh(140, function(b) {
                for (var c = [], d = 0; d < arguments.length; ++d) c[d - 0] = arguments[d];
                return a.K.apply(a, sa(c))
            }), 100), a.B = Eh(J, "resize", function(b) {
                for (var c = [], d = 0; d < arguments.length; ++d) c[d - 0] = arguments[d];
                null !== a.v && a.v.Lc.apply(a.v, sa(c))
            }, 141)), Pk(a, function(b) {
                for (var c = [], d = 0; d < arguments.length; ++d) c[d - 0] = arguments[d];
                return a.J.apply(a, sa(c))
            }), a.J())
        };
    Nk.prototype.K = function() {
        Rk(this, !1);
        this.w()
    };
    Nk.prototype.w = function() {
        Sk(this, Tk(this), !1)
    };
    var Uk = function(a) {
        var b = N.C();
        b.g || "exc" == b.R || Rk(a, !0);
        var c = new Mk;
        switch (b.R) {
            case "geo":
                a: {
                    b = b.l;c = new Mk;c.h = b;
                    if (null != b && -12245933 != b.width && -12245933 != b.height) {
                        var d = N.C();
                        if (d.g) var e = d.o;
                        else try {
                            d = J;
                            var f = a.F;
                            d = d.top;
                            var g = b || dg(!0, d, void 0 === f ? !1 : f),
                                k = jd(cd(d.document).g);
                            if (-12245933 == g.width) {
                                var m = g.width;
                                var l = new I(m, m, m, m)
                            } else l = new I(k.y, k.x + g.width, k.y + g.height, k.x);
                            e = l
                        } catch (t) {
                            a = c;
                            break a
                        }
                        c.g = e
                    }
                    a = c
                }
                return a;
            default:
                return c
        }
    };
    Nk.prototype.V = function() {
        Sk(this, Tk(this), !1)
    };
    var Sk = function(a, b, c, d) {
            if (!a.done && (a.o.cancel(), 0 != b.length)) {
                a.I = null;
                var e = Uk(a);
                try {
                    var f = O(),
                        g = N.C();
                    g.J = f;
                    if (null != Ck.C().g)
                        for (d = 0; d < b.length; d++) ij(b[d], f, c);
                    else switch (g.R) {
                        case "exc":
                            for (d = 0; d < b.length; d++) gj(b[d], f, c);
                            break;
                        case "nis":
                            for (e = 0; e < b.length; e++) p(d) ? b[e].Qb(d) : b[e].ad(f);
                            break;
                        case "gsv":
                            for (e = 0; e < b.length; e++) p(d) ? b[e].Pc(d) : b[e].$c(f);
                            break;
                        case "aio":
                        case "nio":
                            for (d = 0; d < b.length; d++) gj(b[d], f, c);
                            break;
                        case "iem":
                            for (d = 0; d < b.length; d++) dj(b[d], f, c);
                            break;
                        case "geo":
                            if (e.g)
                                for (d =
                                    0; d < b.length; d++) fj(b[d], f, e.g, c, g.h)
                    }
                    for (d = 0; d < b.length; d++);
                    a.l.pd += O() - f;
                    ++a.l.Qd
                } finally {
                    c ? z(b, function(a) {
                        a.Z.g = 0
                    }) : Th(a.o)
                }
            }
        },
        Pk = function(a, b) {
            var c;
            wf.visibilityState ? c = "visibilitychange" : wf.mozVisibilityState ? c = "mozvisibilitychange" : wf.webkitVisibilityState && (c = "webkitvisibilitychange");
            c && (a.D = a.D || Eh(wf, c, b, 142))
        };
    Nk.prototype.J = function() {
        var a = Vk(this),
            b = O();
        a ? (Lg || (Hg = b, z(this.g, function(a) {
            var c = a.na();
            c.K = Xj(c, b, 1 != a.$)
        })), Lg = !0, Rk(this, !0)) : (Lg = !1, Jg = b, z(this.g, function(a) {
            a.bb && (a.na().B = b)
        }));
        Sk(this, Tk(this), !a)
    };
    Nk.prototype.O = function(a) {
        var b;
        if (b = null != a.IntersectionObserver) {
            if (a = Wk(a, Tk(this))) N.C().R = "nio";
            b = a
        }
        return b
    };
    Nk.prototype.L = function(a) {
        return pc && Dc(8) && v(pi(a && a.document)) ? (N.C().R = "iem", !0) : !1
    };
    var Vk = function(a) {
            if (Xk(a)) return !0;
            var b = Ih(wf);
            a = 1 === b;
            b = 0 === b;
            return N.C(), a || b
        },
        Yk = function(a, b) {
            return null != b && nb(a.g, function(a) {
                return a.element == b
            })
        },
        Zk = function(a) {
            return pb(P.g, function(b) {
                return b.ia == a
            })
        },
        Tk = function(a) {
            return 0 == a.g.length ? a.h : 0 == a.h.length ? a.g : wb(a.h, a.g)
        };
    Nk.prototype.reset = function() {
        this.g = [];
        this.h = []
    };
    var Rk = function(a, b) {
            a = a.F;
            var c = N.C(),
                d, e = Ck.C();
            null != e.g && (d = e.g.g);
            c.l = d ? d.g().h : c.g ? c.o ? (new E(c.o.h(), c.o.g())).round() : new E(0, 0) : dg(!0, J, a);
            b || (c.F = J && J.outerWidth ? new E(J.outerWidth, J.outerHeight) : new E(-12245933, -12245933), c.v = Ph(c.l))
        },
        $k = function() {
            var a = N.C();
            J.screen && (a.w = new E(J.screen.width, J.screen.height))
        },
        Wk = function(a, b) {
            var c = void 0 === c ? Da : c;
            var d = !1;
            z(b, function(b) {
                oj(b, a, "nio", c) && (d = !0)
            });
            return d
        },
        al = function(a) {
            var b = P,
                c = [];
            z(a, function(a) {
                Yk(b, a.element) || (b.g.push(a),
                    c.push(a))
            })
        },
        bl = function(a) {
            var b = P,
                c = [];
            z(a, function(a) {
                null == pb(b.g, function(b) {
                    return b.element == a.element && !0
                }) && (b.g.push(a), c.push(a))
            })
        },
        Xk = function(a) {
            return nb(Tk(a), function(a) {
                return a.Ca
            })
        };
    Ea(Nk);
    var P = Nk.C();
    var cl = function() {
            var a = C;
            return a ? nb("AppleTV;GoogleTV;HbbTV;NetCast.TV;Opera TV;POV_TV;SMART-TV;SmartTV;TV Store;OMI/".split(";"), function(b) {
                return B(a, b)
            }) ? !0 : B(a, "Presto") && B(a, "Linux") && !B(a, "X11") && !B(a, "Android") && !B(a, "Mobi") : !1
        },
        dl = function() {
            return B(C, "CrKey") || B(C, "PlayStation") || B(C, "Roku") || cl() || B(C, "Xbox")
        };
    var el = null,
        fl = "",
        gl = !1,
        hl = function(a) {
            if (!a) return "";
            var b = [];
            if (!a.location.href) return "";
            b.push("url=" + encodeURIComponent(a.location.href.substring(0, 512)));
            a.document && a.document.referrer && b.push("referrer=" + encodeURIComponent(a.document.referrer.substring(0, 512)));
            return b.join("&")
        };
    var il = function(a) {
            return function(b) {
                return !p(b[a]) && p(0) ? 0 : b[a]
            }
        },
        kl = function() {
            var a = [0, 2, 4];
            return function(b) {
                b = b.tos;
                if (Ga(b)) {
                    for (var c = Array(b.length), d = 0; d < b.length; d++) c[d] = 0 < d ? c[d - 1] + b[d] : b[d];
                    return p(a) ? jl(c, a) : c
                }
            }
        },
        ll = function(a, b) {
            return function(c) {
                c = c[a];
                if (Ga(c)) return jl(c, b)
            }
        },
        nl = function(a) {
            var b = ml;
            return function(c) {
                return b(c) ? c[a] : void 0
            }
        },
        jl = function(a, b) {
            return jb(a, function(a, d) {
                return rb(b, d)
            })
        };
    var ml = function(a, b) {
            return function(c) {
                for (var d = 0; d < b.length; d++)
                    if (b[d] === c[a] || !p(b[d]) && !c.hasOwnProperty(a)) return !0;
                return !1
            }
        }("e", [void 0, 1, 2, 3, 4, 8, 16]),
        ol = {
            sv: "sv",
            cb: "cb",
            e: "e",
            nas: "nas",
            msg: "msg",
            "if": "if",
            sdk: "sdk",
            p: "p",
            tos: "tos",
            mtos: "mtos",
            mcvt: "mcvt",
            ps: "ps",
            scs: "scs",
            bs: "bs",
            pt: "pt",
            vht: "vht",
            mut: "mut",
            a: "a",
            ft: "ft",
            dft: "dft",
            at: "at",
            dat: "dat",
            as: "as",
            vpt: "vpt",
            gmm: "gmm",
            std: "std",
            efpf: "efpf",
            swf: "swf",
            nio: "nio",
            px: "px",
            nnut: "nnut",
            vmer: "vmer",
            vmmk: "vmmk",
            vmiec: "vmiec",
            nmt: "nmt",
            tcm: "tcm",
            bt: "bt",
            pst: "pst",
            vpaid: "vpaid",
            dur: "dur",
            vmtime: "vmtime",
            dtos: "dtos",
            dtoss: "dtoss",
            dvs: "dvs",
            dfvs: "dfvs",
            dvpt: "dvpt",
            fmf: "fmf",
            vds: "vds",
            is: "is",
            i0: "i0",
            i1: "i1",
            i2: "i2",
            i3: "i3",
            ic: "ic",
            cs: "cs",
            c: "c",
            mc: "mc",
            nc: "nc",
            mv: "mv",
            nv: "nv",
            qmt: nl("qmtos"),
            qnc: nl("qnc"),
            qmv: nl("qmv"),
            qnv: nl("qnv"),
            raf: "raf",
            rafc: "rafc",
            lte: "lte",
            ces: "ces",
            tth: "tth",
            femt: "femt",
            femvt: "femvt",
            emc: "emc",
            emuc: "emuc",
            emb: "emb",
            avms: "avms",
            nvat: "nvat",
            qi: "qi",
            psm: "psm",
            psv: "psv",
            psfv: "psfv",
            psa: "psa",
            pnk: "pnk",
            pnc: "pnc",
            pnmm: "pnmm",
            pns: "pns",
            ptlt: "ptlt",
            dc_rfl: "urlsigs",
            pngs: "pings",
            obd: "obd",
            veid: "veid",
            ssb: "ssb"
        },
        pl = {
            c: il("c"),
            at: "at",
            atos: ll("atos", [0, 2, 4]),
            ta: function(a, b) {
                return function(c) {
                    if (!p(c[a])) return b
                }
            }("tth", "1"),
            a: "a",
            dur: "dur",
            p: "p",
            tos: kl(),
            j: "dom",
            mtos: ll("mtos", [0, 2, 4]),
            gmm: "gmm",
            gdr: "gdr",
            ss: il("ss"),
            vsv: ce("w2"),
            t: "t"
        },
        ql = {
            atos: "atos",
            amtos: "amtos",
            avt: ll("atos", [2]),
            davs: "davs",
            dafvs: "dafvs",
            dav: "dav",
            ss: il("ss"),
            t: "t"
        },
        rl = {
            a: "a",
            tos: kl(),
            at: "at",
            c: il("c"),
            mtos: ll("mtos", [0, 2, 4]),
            dur: "dur",
            fs: "fs",
            p: "p",
            vpt: "vpt",
            vsv: ce("ias_w2"),
            dom: "dom",
            gmm: "gmm",
            gdr: "gdr",
            t: "t"
        },
        sl = {
            tos: kl(),
            at: "at",
            c: il("c"),
            mtos: ll("mtos", [0, 2, 4]),
            p: "p",
            vpt: "vpt",
            vsv: ce("dv_w4"),
            gmm: "gmm",
            gdr: "gdr",
            dom: "dom",
            t: "t",
            mv: "mv",
            qmpt: ll("qmtos", [0, 2, 4]),
            qvs: function(a, b) {
                return function(c) {
                    var d = c[a];
                    if (r(d)) return kb(b, function(a) {
                        return 0 < d && d >= a ? 1 : 0
                    })
                }
            }("qnc", [1, .5, 0]),
            qmv: "qmv",
            qa: "qas",
            a: "a"
        };
    var ul = function(a, b) {
            var c = {
                sv: "654",
                cb: "j"
            };
            c.nas = P.g.length;
            c.msg = a;
            p(b) && (a = tl(b)) && (c.e = Kj[a]);
            return c
        },
        vl = function(a) {
            return 0 == a.lastIndexOf("custom_metric_viewable", 0)
        },
        tl = function(a) {
            var b = vl(a) ? "custom_metric_viewable" : a.toLowerCase();
            return $a(function(a) {
                return a == b
            })
        };
    var wl = function(a, b) {
        Zj.call(this, a, b)
    };
    ia(wl, Zj);
    wl.prototype.l = function(a) {
        return a.na().Xa()
    };
    var xl = function() {
        this.h = this.o = this.A = this.v = this.l = this.g = ""
    };
    var yl = function() {},
        zl = function(a, b, c, d, e) {
            var f = {};
            if (p(a))
                if (null != b)
                    for (var g in b) {
                        var k = b[g];
                        g in Object.prototype || null != k && (v(k) ? f[g] = k(a) : f[g] = a[k])
                    } else eb(f, a);
            p(c) && eb(f, c);
            a = ji(ii(li(f)));
            0 < a.length && p(d) && p(e) && (e = e(a), a += "&" + d + "=" + e);
            return a
        };
    var Al = function() {};
    ia(Al, yl);
    Al.prototype.g = function(a) {
        var b = new xl;
        b.g = zl(a, ol);
        b.l = zl(a, ql);
        return b
    };
    var Cl = function(a) {
        a = Bl(a);
        ei.call(this, a.length ? a[a.length - 1] : new Wh(window, 0));
        this.l = a;
        this.o = Da;
        this.h = null
    };
    ia(Cl, ei);
    h = Cl.prototype;
    h.La = function() {
        return (this.h ? this.h : this.g).La()
    };
    h.Aa = function() {
        return (this.h ? this.h : this.g).Aa()
    };
    h.Ba = function() {
        return (this.h ? this.h : this.g).Ba()
    };
    h.dd = function(a) {
        this.o = a;
        z(this.l, function(a) {
            return a.uc()
        });
        Zh(this.g, this);
        return !0
    };
    h.W = function() {
        z(this.l, function(a) {
            a.B();
            a.W()
        });
        ei.prototype.W.call(this)
    };
    h.Ya = function() {
        return nb(this.l, function(a) {
            return a.Ya()
        })
    };
    h.za = function() {
        return nb(this.l, function(a) {
            return a.za()
        })
    };
    h.Jc = function(a, b, c) {
        return new Si(a, this.g, b, c)
    };
    h.Ta = function(a) {
        0 == a.Ba() && this.o(a.fb(), this)
    };
    h.Ja = function(a) {
        this.h = a.v
    };
    h.Ea = function() {
        return !1
    };
    var Bl = function(a) {
        if (!a.length) return [];
        a = jb(a, function(a) {
            return null != a && a.Ya()
        });
        for (var b = 1; b < a.length; b++) Zh(a[b - 1], a[b]);
        return a
    };
    var Dl = function() {
        Wh.call(this, J, 1, "osd");
        this.X = [];
        this.U = this.F = this.H = 0;
        this.G = !0
    };
    ia(Dl, Wh);
    Dl.prototype.Aa = function() {
        return {
            pi: 1
        }
    };
    var El = function(a) {
        var b = 0;
        a = a.aa;
        try {
            if (a && a.Goog_AdSense_getAdAdapterInstance) return a
        } catch (c) {}
        for (; a && 5 > b;) {
            try {
                if (a.google_osd_static_frame) return a.google_osd_static_frame
            } catch (c) {}
            try {
                if (a.aswift_0 && a.aswift_0.google_osd_static_frame) return a.aswift_0.google_osd_static_frame
            } catch (c) {}
            b++;
            a = a != a.parent ? a.parent : null
        }
        return null
    };
    Dl.prototype.uc = function() {
        var a = this;
        if (!this.J)
            if (this.J = !0, Og()) {
                Eh(n, "message", function(b) {
                    if (null != b && b.data && q(b.data)) {
                        var c = b.data;
                        if (q(c)) {
                            var e = {};
                            c = c.split("\n");
                            for (var f = 0; f != c.length; ++f) {
                                var g = c[f],
                                    k = g.indexOf("=");
                                if (!(0 >= k)) {
                                    var m = Number(g.substr(0, k));
                                    g = g.substr(k + 1);
                                    switch (m) {
                                        case 36:
                                        case 26:
                                        case 15:
                                        case 8:
                                        case 11:
                                        case 16:
                                        case 5:
                                        case 18:
                                            g = "true" == g;
                                            break;
                                        case 4:
                                        case 33:
                                        case 6:
                                        case 25:
                                        case 28:
                                        case 29:
                                        case 24:
                                        case 23:
                                        case 22:
                                        case 7:
                                        case 21:
                                        case 20:
                                            g = Number(g);
                                            break;
                                        case 19:
                                        case 3:
                                            if (v(decodeURIComponent)) try {
                                                g =
                                                    decodeURIComponent(g)
                                            } catch (t) {
                                                throw Error("Error: URI malformed: " + g);
                                            }
                                    }
                                    e[m] = g
                                }
                            }
                            e = e[0] ? e : null
                        } else e = null;
                        if (e && (c = new Ng(e[4], e[12]), N.C().A.matches(c) && (c = e[29], f = e[0], rb(["goog_acknowledge_monitoring", "goog_get_mode", "goog_update_data", "goog_image_request"], f)))) {
                            Fl(a, e);
                            if ("goog_get_mode" == f && b.source) {
                                m = {};
                                Fh(m);
                                m[0] = "goog_provide_mode";
                                m[6] = 4;
                                m[27] = a.aa.document.domain;
                                m[16] = !1;
                                try {
                                    var l = Gh(m);
                                    b.source.postMessage(l, b.origin);
                                    Gl(a, l)
                                } catch (t) {
                                    Ch(406, t)
                                }
                            }
                            if ("goog_get_mode" == f || "goog_acknowledge_monitoring" ==
                                f) a.H = 2, Hl(a);
                            if (b = e[32]) a.P = b;
                            a.l.length && 4 != c && (c = !1, b = a.o.h, l = a.o.g, "goog_acknowledge_monitoring" == e[0] && (a.v = (void 0 !== e[36] ? e[36] : !e[8]) ? 2 : 0, Xh(a)), isNaN(e[30]) || isNaN(e[31]) ? isNaN(e[22]) || isNaN(e[23]) || (c = !0, b = new E(e[22], e[23])) : (c = !0, b = new E(e[30], e[31])), e[9] && (c = !0, e = e[9].split("-"), 4 == e.length && (l = new I(Ub(e[0]), Ub(e[3]), Ub(e[2]), Ub(e[1])))), c && (e = O(), c = dg(!0, a.aa, a.O), f = dg(!1, a.aa, a.O), a.I || (a.I = Ph(c, a.aa)), g = Ih(wf), m = 1 === g, g = 0 === g, m = (N.C(), m || g), e = new Qh(e, c, f, m, a), e.h = b, e.g = l, e.o = Vk(P),
                                ci(a, e)))
                        }
                    }
                }, 118);
                var b = Bh(197, function() {
                    ++a.U;
                    if (2 == a.H) Hl(a);
                    else if (10 < a.U) Yh(a, "no");
                    else if (a.aa.postMessage)
                        if (Og()) {
                            var b = El(a);
                            if (b) {
                                var d = {};
                                Fh(d);
                                d[0] = "goog_request_monitoring";
                                d[6] = 4;
                                d[27] = a.aa.document.domain;
                                d[16] = !1;
                                try {
                                    var e = Gh(d);
                                    b.postMessage(e, "*")
                                } catch (f) {}
                            }
                        } else Yh(a, "ib");
                    else Yh(a, "c")
                });
                this.H = 1;
                1 == Cg(N.C().M, "srmi") && b();
                this.F = this.aa.setInterval(b, 500)
            } else Yh(this, "ib")
    };
    Dl.prototype.B = function() {
        var a = {};
        Fh(a);
        a[0] = "goog_stop_monitoring";
        Gl(this, Gh(a));
        Hl(this)
    };
    var Hl = function(a) {
            a.aa.clearInterval(a.F);
            a.F = 0
        },
        Gl = function(a, b) {
            var c = El(a),
                d = !c;
            d && (c = a.aa.parent);
            if (c && c.postMessage) try {
                c.postMessage(b, "*"), d && a.aa.postMessage(b, "*")
            } catch (e) {}
        },
        Fl = function(a, b) {
            z(a.X, function(a) {
                a(b)
            })
        };
    Dl.prototype.Ya = function() {
        var a = N.C();
        return Cg(a.M, "osd") && this.za() ? 4 === a.H ? !!Cg(a.M, "mkm") : !0 : !1
    };
    Dl.prototype.za = function() {
        return N.C().h
    };
    Ea(Dl);
    var Jl = function() {
        this.h = this.L = !1;
        this.l = null;
        this.o = new Al;
        this.g = null;
        var a = {};
        this.I = (a.start = this.fe, a.firstquartile = this.ae, a.midpoint = this.ce, a.thirdquartile = this.ge, a.complete = this.Zd, a.pause = this.xc, a.resume = this.nd, a.skip = this.ee, a.viewable_impression = this.Ia, a.mute = this.ob, a.unmute = this.ob, a.fullscreen = this.be, a.exitfullscreen = this.$d, a.fully_viewable_audible_half_duration_impression = this.Ia, a.measurable_impression = this.Ia, a.abandon = this.xc, a.engagedview = this.Ia, a.impression = this.Ia, a.creativeview =
            this.Ia, a.progress = this.ob, a.custom_metric_viewable = this.Ia, a.bufferstart = this.xc, a.bufferfinish = this.nd, a);
        a = {};
        this.P = (a.overlay_resize = this.de, a.abandon = this.bc, a.close = this.bc, a.collapse = this.bc, a.overlay_unmeasurable_impression = function(a) {
                return sk(a, "overlay_unmeasurable_impression", Vk(P))
            }, a.overlay_viewable_immediate_impression = function(a) {
                return sk(a, "overlay_viewable_immediate_impression", Vk(P))
            }, a.overlay_unviewable_impression = function(a) {
                return sk(a, "overlay_unviewable_impression", Vk(P))
            },
            a.overlay_viewable_end_of_session_impression = function(a) {
                return sk(a, "overlay_viewable_end_of_session_impression", Vk(P))
            }, a);
        N.C().H = 3;
        Il(this)
    };
    Jl.prototype.A = function(a) {
        a.Ca = !1;
        Kl(a.sa(), a.ia)
    };
    Jl.prototype.B = function() {};
    var Ll = function(a, b, c, d) {
        b = a.w(null, d, !0, b);
        b.o = c;
        b.sc = function(b) {
            a.J(b)
        };
        al([b]);
        return b
    };
    Jl.prototype.w = function(a, b, c, d) {
        this.g || (this.g = this.Kc());
        b = c ? b : -1;
        if (null == this.g || this.h) return a = new fk(J, a, b, 7), a.ia = d, a;
        a = new fk(J, a, b, 7, new Zj("measurable_impression", this.g), this.H());
        a.ia = d;
        return a
    };
    Jl.prototype.H = function() {
        return [new wl("viewable_impression", this.g)]
    };
    var Ml = function() {
            var a = [],
                b = N.C();
            Cg(b.M, "osd") && b.h && b.g && "exc" != b.R && (N.C().g = !1, a.push(Dl.C()));
            return [new Jk(J), new Cl(a)]
        },
        Ol = function(a) {
            if (!a.L) {
                a.L = !0;
                try {
                    var b = O(),
                        c = N.C();
                    Ig = b;
                    el = tj(J).aa;
                    Rk(P, !1);
                    $k();
                    if ("nis" != c.R && "gsv" != c.R)
                        if (J.document.body && J.document.body.getBoundingClientRect) {
                            P.l.Md = 0;
                            P.l.Gf = O() - b;
                            var d = Ml(),
                                e = Ck.C();
                            e.h = d;
                            if (Dk(e, function() {
                                    c.g = !1;
                                    Nl()
                                })) P.done || (Qk(), Zh(e.g.g, a));
                            else if (c.h && "exc" != c.R) {
                                var f = !!Cg(c.M, "osd");
                                if (c.g && !f) {
                                    var g = Dl.C();
                                    g.uc();
                                    Zh(g, a)
                                } else Nl()
                            } else Qk()
                        } else gl = !0
                } catch (k) {
                    throw P.reset(), k;
                }
            }
        },
        Pl = function(a) {
            var b = N.C();
            if (null == a.l) switch (b.R) {
                case "nis":
                    a.l = "n";
                    break;
                case "gsv":
                    a.l = "m";
                    break;
                default:
                    a.l = "h"
            }
            return a.l
        },
        Ql = function(a, b, c) {
            if (null == a.g) return b.pb |= 4, !1;
            a = ak(a.g, c, b);
            b.pb |= a;
            return 0 == a
        };
    Jl.prototype.Ta = function(a) {
        var b = N.C();
        switch (a.Ba()) {
            case 0:
                b.g = !1;
                (a = Ck.C().g) && ai(a.g, this);
                (a = Dl.C()) && ai(a, this);
                Nl();
                break;
            case 2:
                b.g && Qk()
        }
    };
    Jl.prototype.Ja = function(a) {
        var b = N.C();
        b.g && (b.l = a.h, b.o = a.g)
    };
    Jl.prototype.Ea = function() {
        return !1
    };
    var Nl = function() {
        a: {
            var a = P;
            if (void 0 === b) {
                var b = N.C().M;
                var c = [];
                0 === (Cg(b, "nio_mode") || 0) && c.push(a.O);
                c.push(a.L);
                b = c
            }
            b = ra(b);
            for (c = b.next(); !c.done; c = b.next())
                if (c.value.call(a, J)) {
                    a = !0;
                    break a
                }
            a = !1
        }
        a ? Qk() : (P.o.cancel(), fl = "i", P.done = !0)
    };
    Jl.prototype.O = function(a, b) {
        a.jb = !0;
        switch (a.sa()) {
            case 1:
                Rl(this, a, b);
                break;
            case 2:
                this.yc(a)
        }
        this.zc(a)
    };
    var Rl = function(a, b, c) {
        if (!b.Pa) {
            var d = sk(b, "start", Vk(P));
            a = a.o.g(d).g;
            d = el || J;
            var e = [];
            e.push("v=654v");
            e.push("r=" + c);
            e.push(a);
            e.push(hl(d));
            c = ("//pagead2.googlesyndication.com/pagead/gen_204?id=lidarvf&" + e.join("&")).toString();
            c = c.substring(0, 4E3);
            a = cg() || J;
            Yf(a, c, void 0);
            b.Pa = !0
        }
    };
    h = Jl.prototype;
    h.fe = function(a) {
        pk(a, 0);
        return sk(a, "start", Vk(P))
    };
    h.ob = function(a, b, c) {
        Sk(P, [a], !Vk(P), b);
        return this.Ia(a, b, c)
    };
    h.Ia = function(a, b, c) {
        return sk(a, c, Vk(P))
    };
    h.ae = function(a, b) {
        return Sl(a, "firstquartile", 1, b)
    };
    h.ce = function(a, b) {
        a.ma = !0;
        return Sl(a, "midpoint", 2, b)
    };
    h.ge = function(a, b) {
        return Sl(a, "thirdquartile", 3, b)
    };
    h.Zd = function(a, b) {
        b = Sl(a, "complete", 4, b);
        0 != a.$ && (a.$ = 3);
        return b
    };
    var Sl = function(a, b, c, d) {
        Sk(P, [a], !Vk(P), d);
        pk(a, c);
        4 != c && ok(a.G, c, a.Hb);
        return sk(a, b, Vk(P))
    };
    h = Jl.prototype;
    h.nd = function(a, b, c) {
        var d = Vk(P);
        if (2 == a.$ && !d) {
            var e = O();
            a.na().B = e
        }
        Sk(P, [a], !d, b);
        2 == a.$ && (a.$ = 1);
        return sk(a, c, d)
    };
    h.ee = function(a, b) {
        b = this.ob(a, b || {}, "skip");
        0 != a.$ && (a.$ = 3);
        return b
    };
    h.be = function(a, b) {
        a.Ca = !0;
        return this.ob(a, b || {}, "fullscreen")
    };
    h.$d = function(a, b) {
        a.Ca = !1;
        return this.ob(a, b || {}, "exitfullscreen")
    };
    h.xc = function(a, b, c) {
        var d = a.na(),
            e = O();
        d.K = Xj(d, e, 1 != a.$);
        Sk(P, [a], !Vk(P), b);
        1 == a.$ && (a.$ = 2);
        return sk(a, c, Vk(P))
    };
    h.de = function(a, b) {
        Sk(P, [a], !Vk(P), b);
        return a.h()
    };
    h.bc = function(a, b) {
        Sk(P, [a], !Vk(P), b);
        this.ld(a);
        0 != a.$ && (a.$ = 3);
        return a.h()
    };
    var Tl = function(a, b, c) {
            if (0 == b.$) {
                "i" != fl && (P.done = !1);
                var d = Ck.C();
                null != d.g && hj(b, d.g);
                oj(b, J, N.C().R, function(b) {
                    for (var c = [], d = 0; d < arguments.length; ++d) c[d - 0] = arguments[d];
                    return a.O.apply(a, sa(c))
                });
                d = p(c) ? c.opt_nativeTime : void 0;
                Kg = d = r(d) ? d : O();
                b.bb = !0;
                var e = Vk(P);
                gk(b, d, e);
                Sk(P, [b], !e, c)
            }
        },
        Il = function(a) {
            yh([function(b) {
                var c = Ul();
                null != a.l && (c.sdk = a.l);
                c.avms = N.C().R;
                eb(b, c)
            }])
        },
        Kl = function(a, b) {
            if (q(b)) {
                if (1 == a) var c = P.g;
                else if (2 == a) c = P.h;
                else return;
                var d = ob(c, function(c) {
                    return c.sa() !=
                        a ? !1 : c.ia == b
                });
                0 <= d && (kj(c[d]), sb(c, d))
            }
        },
        Wl = function(a, b, c, d) {
            var e = pb(P.g, function(a) {
                return a.element == c
            });
            null !== e && e.ia != b && (a.A(e), e = null);
            e || (e = Vl(a, c, b), e.o = Pl(a), d && (e.Ma = d));
            return e
        },
        Vl = function(a, b, c) {
            b = a.w(b, O(), !1, c);
            b.sc = w(a.J, a);
            0 == P.h.length && (N.C().B = 79463069);
            bl([b]);
            Qk();
            return b
        };
    Jl.prototype.J = function() {};
    var Yl = function(a, b) {
        b.H = 0;
        for (var c in Gj) null == a[c] && (b.H |= Gj[c]);
        Xl(a, "currentTime");
        Xl(a, "duration")
    };
    h = Jl.prototype;
    h.yc = function() {};
    h.ld = function() {};
    h.cd = function() {};
    h.zc = function() {};
    h.Kc = function() {};
    var Xl = function(a, b) {
            var c = a[b];
            p(c) && 0 < c && (a[b] = Math.floor(1E3 * c))
        },
        Ul = function() {
            var a = N.C(),
                b = {};
            return b.sv = "654", b["if"] = a.h ? "1" : "0", b.nas = String(P.g.length), b
        };
    var Zl = {
            Og: "visible",
            sg: "audible",
            fi: "time",
            hi: "timetype"
        },
        $l = {
            visible: function(a) {
                return /^(100|[0-9]{1,2})$/.test(a)
            },
            audible: function(a) {
                return "0" == a || "1" == a
            },
            timetype: function(a) {
                return "mtos" == a || "tos" == a
            },
            time: function(a) {
                return /^(100|[0-9]{1,2})%$/.test(a) || /^([0-9])+ms$/.test(a)
            }
        },
        am = function() {
            this.g = void 0;
            this.h = !1;
            this.l = 0;
            this.o = -1;
            this.v = "tos"
        },
        bm = function(a) {
            try {
                var b = a.split(",");
                return b.length > Va(Zl).length ? null : lb(b, function(a, b) {
                    b = b.toLowerCase().split("=");
                    if (2 != b.length || !p($l[b[0]]) ||
                        !$l[b[0]](b[1])) throw Error("Entry (" + b[0] + ", " + b[1] + ") is invalid.");
                    a[b[0]] = b[1];
                    return a
                }, {})
            } catch (c) {
                return null
            }
        },
        cm = function(a, b) {
            if (void 0 == a.g) return 0;
            switch (a.v) {
                case "mtos":
                    return a.h ? Mi(b.h, a.g) : Mi(b.g, a.g);
                case "tos":
                    return a.h ? xi(b.h, a.g) : xi(b.g, a.g)
            }
            return 0
        };
    var dm = function(a, b, c, d) {
        Zj.call(this, b, d);
        this.B = a;
        this.w = c
    };
    ia(dm, Zj);
    dm.prototype.A = function() {
        return this.B
    };
    dm.prototype.v = function() {
        return !0
    };
    dm.prototype.l = function(a) {
        var b = a.na(),
            c = a.v;
        return nb(this.w, function(a) {
            if (void 0 != a.g) var d = cm(a, b);
            else b: {
                switch (a.v) {
                    case "mtos":
                        d = a.h ? b.v.l : b.l.g;
                        break b;
                    case "tos":
                        d = a.h ? b.v.g : b.l.g;
                        break b
                }
                d = 0
            }
            0 == d ? a = !1 : (a = -1 != a.l ? a.l : p(c) && 0 < c ? a.o * c : -1, a = -1 != a && d >= a);
            return a
        })
    };
    var em = function(a) {
        Zj.call(this, "fully_viewable_audible_half_duration_impression", a)
    };
    ia(em, Zj);
    em.prototype.l = function(a) {
        var b = xi(a.na().h, 1);
        return jk(a, b)
    };
    var fm = x(),
        gm = !1,
        hm = !1,
        im = !1,
        Q = function(a) {
            return !a || "function" !== typeof a || 0 > String(Function.prototype.toString).indexOf("[native code]") ? !1 : 0 <= String(a).indexOf("[native code]") && !0 || !1
        },
        jm = function(a) {
            return !!(1 << a & fm)
        },
        km = [function(a) {
                return !(!a.chrome || !a.chrome.webstore)
            }, function(a) {
                return !!a.document.documentMode
            }, function(a) {
                return !!a.document.fonts.ready
            }, function() {
                return jm(0)
            }, function(a) {
                return !!a.ActiveXObject
            }, function(a) {
                return !!a.chrome
            }, function(a) {
                return !!a.navigator.serviceWorker
            },
            function(a) {
                return !!a.opera
            },
            function(a) {
                return !!a.sidebar
            },
            function() {
                return !+"\v1"
            },
            function() {
                return jm(1)
            },
            function(a) {
                return !a.ActiveXObject
            },
            function(a) {
                return "-ms-ime-align" in a.document.documentElement.style
            },
            function(a) {
                return "-ms-scroll-limit" in a.document.documentElement.style
            },
            function(a) {
                return "-webkit-font-feature-settings" in a.document.body.style
            },
            function() {
                return jm(2)
            },
            function(a) {
                return "ActiveXObject" in a
            },
            function(a) {
                return "MozAppearance" in a.document.documentElement.style
            },
            function(a) {
                return "_phantom" in
                    a
            },
            function(a) {
                return "callPhantom" in a
            },
            function(a) {
                return "content" in a.document.createElement("template")
            },
            function(a) {
                return "getEntriesByType" in a.performance
            },
            function() {
                return jm(3)
            },
            function(a) {
                return "image-rendering" in a.document.body.style
            },
            function(a) {
                return "object-fit" in a.document.body.style
            },
            function(a) {
                return "open" in a.document.createElement("details")
            },
            function(a) {
                return "orientation" in a.screen
            },
            function(a) {
                return "performance" in a
            },
            function(a) {
                return "shape-image-threshold" in a.document.body.style
            },
            function() {
                return jm(4)
            },
            function(a) {
                return "srcset" in a.document.createElement("img")
            },
            function() {
                return hm
            },
            function() {
                return im
            },
            function() {
                return jm(5)
            },
            function(a) {
                a = a.document.createElement("div");
                a.style.width = "1px";
                a.style.width = "-webkit-min-content";
                a.style.width = "min-content";
                return "1px" != a.style.width
            },
            function(a) {
                a = a.document.createElement("div");
                a.style.width = "1px";
                a.style.width = "calc(1px - 1px)";
                a.style.width = "-webkit-calc(1px - 1px)";
                return "1px" != a.style.width
            },
            function() {
                var a = !1;
                eval('var DummyFunction1 = function(x){ "use strict"; var a = 12; b = a + x*35; }');
                try {
                    DummyFunction1()
                } catch (b) {
                    a = !0
                }
                return a
            },
            function() {
                var a = !1;
                try {
                    DummyFunction2()
                } catch (b) {
                    a = !0
                }
                return a
            },
            function() {
                return !1
            },
            function() {
                return jm(6)
            },
            function(a) {
                var b = a.document.createElement("canvas");
                b.width = b.height = 1;
                b = b.getContext("2d");
                b.globalCompositeOperation = "multiply";
                b.fillStyle = "rgb(0,255,255)";
                b.fillRect(0, 0, 1, 1);
                b.fill();
                b.fillStyle = "rgb(255,255,0)";
                b.fillRect(0, 0, 1, 1);
                b.fill();
                b = b.getImageData(0, 0, 1, 1).data;
                return b[0] == b[2] && b[1] == b[3] || Q(a.navigator.vibrate)
            },
            function(a) {
                a =
                    a.document.createElement("canvas");
                a.width = a.height = 1;
                a = a.getContext("2d");
                a.globalCompositeOperation = "multiply";
                a.fillStyle = "rgb(0,255,255)";
                a.fillRect(0, 0, 1, 1);
                a.fill();
                a.fillStyle = "rgb(255,255,0)";
                a.fillRect(0, 0, 1, 1);
                a.fill();
                a = a.getImageData(0, 0, 1, 1).data;
                return a[0] == a[2] && a[1] == a[3]
            },
            function(a) {
                return Q(a.document.createElement("div").matches)
            },
            function(a) {
                a = a.document.createElement("input");
                a.setAttribute("type", "range");
                return "text" !== a.type
            },
            function(a) {
                return a.CSS.supports("image-rendering",
                    "pixelated")
            },
            function(a) {
                return a.CSS.supports("object-fit", "contain")
            },
            function() {
                return jm(7)
            },
            function(a) {
                return a.CSS.supports("object-fit", "inherit")
            },
            function(a) {
                return a.CSS.supports("shape-image-threshold", "0.9")
            },
            function(a) {
                return a.CSS.supports("word-break", "keep-all")
            },
            function() {
                return eval("1 == [for (item of [1,2,3]) item][0]")
            },
            function(a) {
                return Q(a.CSS.supports)
            },
            function() {
                return Q(Intl.Collator)
            },
            function(a) {
                return Q(a.document.createElement("dialog").show)
            },
            function() {
                return jm(8)
            },
            function(a) {
                return Q(a.document.createElement("div").animate([{
                    transform: "scale(1)",
                    easing: "ease-in"
                }, {
                    transform: "scale(1.3)",
                    easing: "ease-in"
                }], {
                    duration: 1300,
                    iterations: 1
                }).reverse)
            },
            function(a) {
                return Q(a.document.createElement("div").animate)
            },
            function(a) {
                return Q(a.document.documentElement.webkitRequestFullScreen)
            },
            function(a) {
                return Q(a.navigator.getBattery)
            },
            function(a) {
                return Q(a.navigator.permissions.query)
            },
            function() {
                return !1
            },
            function() {
                return jm(9)
            },
            function() {
                return Q(webkitRequestAnimationFrame)
            },
            function(a) {
                return Q(a.BroadcastChannel.call)
            },
            function(a) {
                return Q(a.FontFace)
            },
            function(a) {
                return Q(a.Gamepad)
            },
            function() {
                return jm(10)
            },
            function(a) {
                return Q(a.MutationEvent)
            },
            function(a) {
                return Q(a.MutationObserver)
            },
            function(a) {
                return Q(a.crypto.getRandomValues)
            },
            function(a) {
                return Q(a.document.body.createShadowRoot)
            },
            function(a) {
                return Q(a.document.body.webkitCreateShadowRoot)
            },
            function(a) {
                return Q(a.fetch)
            },
            function() {
                return jm(11)
            },
            function(a) {
                return Q(a.navigator.serviceWorker.register)
            },
            function(a) {
                return Q(a.navigator.webkitGetGamepads)
            },
            function(a) {
                return Q(a.speechSynthesis.speak)
            },
            function(a) {
                return Q(a.webkitRTCPeerConnection)
            },
            function(a) {
                return a.CSS.supports("--fake-var", "0")
            },
            function() {
                return jm(12)
            },
            function(a) {
                return a.CSS.supports("cursor", "grab")
            },
            function(a) {
                return a.CSS.supports("cursor", "zoom-in")
            },
            function(a) {
                return a.CSS.supports("image-orientation", "270deg")
            },
            function() {
                return jm(13)
            },
            function(a) {
                return a.CSS.supports("position", "sticky")
            },
            function(a) {
                return void 0 ===
                    a.document.createElement("style").scoped
            },
            function(a) {
                return a.performance.getEntriesByType("resource") instanceof Array
            },
            function() {
                return "undefined" == typeof InstallTrigger
            },
            function() {
                return "object" == typeof(new Intl.Collator).resolvedOptions()
            },
            function(a) {
                return "boolean" == typeof a.navigator.onLine
            },
            function() {
                return jm(14)
            },
            function(a) {
                return "undefined" == typeof a.navigator.Bi
            },
            function(a) {
                return "number" == typeof a.performance.now()
            },
            function() {
                return 0 == (new Uint16Array(1))[0]
            },
            function(a) {
                return -1 ==
                    a.ActiveXObject.toString().indexOf("native")
            },
            function(a) {
                return -1 == Object.prototype.toString.call(a.HTMLElement).indexOf("Constructor")
            }
        ],
        lm = [function(a) {
                a = a.document.createElement("div");
                var b = null,
                    c = ["{45EA75A0-A269-11D1-B5BF-0000F8051515}", "{3AF36230-A269-11D1-B5BF-0000F8051515}", "{89820200-ECBD-11CF-8B85-00AA005B4383}"];
                try {
                    a.style.behavior = "url(#default#clientcaps)"
                } catch (e) {}
                for (var d = 0; d < c.length; d++) {
                    try {
                        b = a.getComponentVersion(c[d], "componentid").replace(/,/g, ".")
                    } catch (e) {}
                    if (b) return b.split(".")[0]
                }
                return !1
            },
            function() {
                return (new Date).getTimezoneOffset()
            },
            function(a) {
                return (a.innerWidth || a.document.documentElement.clientWidth || a.document.body.clientWidth) / (a.innerHeight || a.document.documentElement.clientHeight || a.document.body.clientHeight)
            },
            function(a) {
                return (a.outerWidth || a.document && a.document.body && a.document.body.offsetWidth) / (a.outerHeight || a.document && a.document.body && a.document.body.offsetHeight)
            },
            function(a) {
                return a.screen.availWidth / a.screen.availHeight
            },
            function(a) {
                return a.screen.width /
                    a.screen.height
            }
        ],
        mm = [function(a) {
            return a.navigator.userAgent
        }, function(a) {
            return a.navigator.platform
        }, function(a) {
            return a.navigator.vendor
        }],
        om = function() {
            try {
                nm()
            } catch (d) {}
            var a = "a=1&b=" + fm + "&",
                b = [],
                c = 99;
            z(km, function(a, c) {
                var d = !1;
                try {
                    d = a(J)
                } catch (g) {}
                b[c / 32 >>> 0] |= d << c % 32
            });
            z(b, function(b, e) {
                a += String.fromCharCode(c + e) + "=" + (b >>> 0).toString(16) + "&"
            });
            c = 105;
            z(lm, function(b) {
                var d = "false";
                try {
                    d = b(J)
                } catch (f) {}
                a += String.fromCharCode(c++) + "=" + d + "&"
            });
            z(mm, function(b) {
                var d = "";
                try {
                    var f = b(J);
                    b = [];
                    for (var g = 0, k = 0; k < f.length; k++) {
                        var m = f.charCodeAt(k);
                        255 < m && (b[g++] = m & 255, m >>= 8);
                        b[g++] = m
                    }
                    if (!ve)
                        for (ve = {}, we = {}, f = 0; 65 > f; f++) ve[f] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(f), we[f] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.".charAt(f);
                    f = we;
                    m = [];
                    for (g = 0; g < b.length; g += 3) {
                        var l = b[g],
                            t = g + 1 < b.length,
                            H = t ? b[g + 1] : 0,
                            fa = g + 2 < b.length,
                            va = fa ? b[g + 2] : 0;
                        k = l >> 2;
                        var La = (l & 3) << 4 | H >> 4,
                            V = (H & 15) << 2 | va >> 6,
                            Yb = va & 63;
                        fa || (Yb = 64, t || (V = 64));
                        m.push(f[k], f[La], f[V], f[Yb])
                    }
                    d =
                        m.join("")
                } catch (Sd) {}
                a += String.fromCharCode(c++) + "=" + d + "&"
            });
            return a.slice(0, -1)
        },
        nm = function() {
            if (!gm) {
                var a = function() {
                    hm = !0;
                    J.document.removeEventListener("webdriver-evaluate", a, !0)
                };
                J.document.addEventListener("webdriver-evaluate", a, !0);
                var b = function() {
                    im = !0;
                    J.document.removeEventListener("webdriver-evaluate-response", b, !0)
                };
                J.document.addEventListener("webdriver-evaluate-response", b, !0);
                gm = !0
            }
        };
    var pm = function() {
        this.h = 64;
        this.g = Array(4);
        this.v = Array(this.h);
        this.o = this.l = 0;
        this.reset()
    };
    y(pm, xe);
    pm.prototype.reset = function() {
        this.g[0] = 1732584193;
        this.g[1] = 4023233417;
        this.g[2] = 2562383102;
        this.g[3] = 271733878;
        this.o = this.l = 0
    };
    var qm = function(a, b, c) {
            c || (c = 0);
            var d = Array(16);
            if (q(b))
                for (var e = 0; 16 > e; ++e) d[e] = b.charCodeAt(c++) | b.charCodeAt(c++) << 8 | b.charCodeAt(c++) << 16 | b.charCodeAt(c++) << 24;
            else
                for (e = 0; 16 > e; ++e) d[e] = b[c++] | b[c++] << 8 | b[c++] << 16 | b[c++] << 24;
            b = a.g[0];
            c = a.g[1];
            e = a.g[2];
            var f = a.g[3];
            var g = b + (f ^ c & (e ^ f)) + d[0] + 3614090360 & 4294967295;
            b = c + (g << 7 & 4294967295 | g >>> 25);
            g = f + (e ^ b & (c ^ e)) + d[1] + 3905402710 & 4294967295;
            f = b + (g << 12 & 4294967295 | g >>> 20);
            g = e + (c ^ f & (b ^ c)) + d[2] + 606105819 & 4294967295;
            e = f + (g << 17 & 4294967295 | g >>> 15);
            g = c + (b ^ e &
                (f ^ b)) + d[3] + 3250441966 & 4294967295;
            c = e + (g << 22 & 4294967295 | g >>> 10);
            g = b + (f ^ c & (e ^ f)) + d[4] + 4118548399 & 4294967295;
            b = c + (g << 7 & 4294967295 | g >>> 25);
            g = f + (e ^ b & (c ^ e)) + d[5] + 1200080426 & 4294967295;
            f = b + (g << 12 & 4294967295 | g >>> 20);
            g = e + (c ^ f & (b ^ c)) + d[6] + 2821735955 & 4294967295;
            e = f + (g << 17 & 4294967295 | g >>> 15);
            g = c + (b ^ e & (f ^ b)) + d[7] + 4249261313 & 4294967295;
            c = e + (g << 22 & 4294967295 | g >>> 10);
            g = b + (f ^ c & (e ^ f)) + d[8] + 1770035416 & 4294967295;
            b = c + (g << 7 & 4294967295 | g >>> 25);
            g = f + (e ^ b & (c ^ e)) + d[9] + 2336552879 & 4294967295;
            f = b + (g << 12 & 4294967295 | g >>> 20);
            g =
                e + (c ^ f & (b ^ c)) + d[10] + 4294925233 & 4294967295;
            e = f + (g << 17 & 4294967295 | g >>> 15);
            g = c + (b ^ e & (f ^ b)) + d[11] + 2304563134 & 4294967295;
            c = e + (g << 22 & 4294967295 | g >>> 10);
            g = b + (f ^ c & (e ^ f)) + d[12] + 1804603682 & 4294967295;
            b = c + (g << 7 & 4294967295 | g >>> 25);
            g = f + (e ^ b & (c ^ e)) + d[13] + 4254626195 & 4294967295;
            f = b + (g << 12 & 4294967295 | g >>> 20);
            g = e + (c ^ f & (b ^ c)) + d[14] + 2792965006 & 4294967295;
            e = f + (g << 17 & 4294967295 | g >>> 15);
            g = c + (b ^ e & (f ^ b)) + d[15] + 1236535329 & 4294967295;
            c = e + (g << 22 & 4294967295 | g >>> 10);
            g = b + (e ^ f & (c ^ e)) + d[1] + 4129170786 & 4294967295;
            b = c + (g << 5 & 4294967295 |
                g >>> 27);
            g = f + (c ^ e & (b ^ c)) + d[6] + 3225465664 & 4294967295;
            f = b + (g << 9 & 4294967295 | g >>> 23);
            g = e + (b ^ c & (f ^ b)) + d[11] + 643717713 & 4294967295;
            e = f + (g << 14 & 4294967295 | g >>> 18);
            g = c + (f ^ b & (e ^ f)) + d[0] + 3921069994 & 4294967295;
            c = e + (g << 20 & 4294967295 | g >>> 12);
            g = b + (e ^ f & (c ^ e)) + d[5] + 3593408605 & 4294967295;
            b = c + (g << 5 & 4294967295 | g >>> 27);
            g = f + (c ^ e & (b ^ c)) + d[10] + 38016083 & 4294967295;
            f = b + (g << 9 & 4294967295 | g >>> 23);
            g = e + (b ^ c & (f ^ b)) + d[15] + 3634488961 & 4294967295;
            e = f + (g << 14 & 4294967295 | g >>> 18);
            g = c + (f ^ b & (e ^ f)) + d[4] + 3889429448 & 4294967295;
            c = e + (g << 20 & 4294967295 |
                g >>> 12);
            g = b + (e ^ f & (c ^ e)) + d[9] + 568446438 & 4294967295;
            b = c + (g << 5 & 4294967295 | g >>> 27);
            g = f + (c ^ e & (b ^ c)) + d[14] + 3275163606 & 4294967295;
            f = b + (g << 9 & 4294967295 | g >>> 23);
            g = e + (b ^ c & (f ^ b)) + d[3] + 4107603335 & 4294967295;
            e = f + (g << 14 & 4294967295 | g >>> 18);
            g = c + (f ^ b & (e ^ f)) + d[8] + 1163531501 & 4294967295;
            c = e + (g << 20 & 4294967295 | g >>> 12);
            g = b + (e ^ f & (c ^ e)) + d[13] + 2850285829 & 4294967295;
            b = c + (g << 5 & 4294967295 | g >>> 27);
            g = f + (c ^ e & (b ^ c)) + d[2] + 4243563512 & 4294967295;
            f = b + (g << 9 & 4294967295 | g >>> 23);
            g = e + (b ^ c & (f ^ b)) + d[7] + 1735328473 & 4294967295;
            e = f + (g << 14 & 4294967295 |
                g >>> 18);
            g = c + (f ^ b & (e ^ f)) + d[12] + 2368359562 & 4294967295;
            c = e + (g << 20 & 4294967295 | g >>> 12);
            g = b + (c ^ e ^ f) + d[5] + 4294588738 & 4294967295;
            b = c + (g << 4 & 4294967295 | g >>> 28);
            g = f + (b ^ c ^ e) + d[8] + 2272392833 & 4294967295;
            f = b + (g << 11 & 4294967295 | g >>> 21);
            g = e + (f ^ b ^ c) + d[11] + 1839030562 & 4294967295;
            e = f + (g << 16 & 4294967295 | g >>> 16);
            g = c + (e ^ f ^ b) + d[14] + 4259657740 & 4294967295;
            c = e + (g << 23 & 4294967295 | g >>> 9);
            g = b + (c ^ e ^ f) + d[1] + 2763975236 & 4294967295;
            b = c + (g << 4 & 4294967295 | g >>> 28);
            g = f + (b ^ c ^ e) + d[4] + 1272893353 & 4294967295;
            f = b + (g << 11 & 4294967295 | g >>> 21);
            g = e + (f ^
                b ^ c) + d[7] + 4139469664 & 4294967295;
            e = f + (g << 16 & 4294967295 | g >>> 16);
            g = c + (e ^ f ^ b) + d[10] + 3200236656 & 4294967295;
            c = e + (g << 23 & 4294967295 | g >>> 9);
            g = b + (c ^ e ^ f) + d[13] + 681279174 & 4294967295;
            b = c + (g << 4 & 4294967295 | g >>> 28);
            g = f + (b ^ c ^ e) + d[0] + 3936430074 & 4294967295;
            f = b + (g << 11 & 4294967295 | g >>> 21);
            g = e + (f ^ b ^ c) + d[3] + 3572445317 & 4294967295;
            e = f + (g << 16 & 4294967295 | g >>> 16);
            g = c + (e ^ f ^ b) + d[6] + 76029189 & 4294967295;
            c = e + (g << 23 & 4294967295 | g >>> 9);
            g = b + (c ^ e ^ f) + d[9] + 3654602809 & 4294967295;
            b = c + (g << 4 & 4294967295 | g >>> 28);
            g = f + (b ^ c ^ e) + d[12] + 3873151461 & 4294967295;
            f = b + (g << 11 & 4294967295 | g >>> 21);
            g = e + (f ^ b ^ c) + d[15] + 530742520 & 4294967295;
            e = f + (g << 16 & 4294967295 | g >>> 16);
            g = c + (e ^ f ^ b) + d[2] + 3299628645 & 4294967295;
            c = e + (g << 23 & 4294967295 | g >>> 9);
            g = b + (e ^ (c | ~f)) + d[0] + 4096336452 & 4294967295;
            b = c + (g << 6 & 4294967295 | g >>> 26);
            g = f + (c ^ (b | ~e)) + d[7] + 1126891415 & 4294967295;
            f = b + (g << 10 & 4294967295 | g >>> 22);
            g = e + (b ^ (f | ~c)) + d[14] + 2878612391 & 4294967295;
            e = f + (g << 15 & 4294967295 | g >>> 17);
            g = c + (f ^ (e | ~b)) + d[5] + 4237533241 & 4294967295;
            c = e + (g << 21 & 4294967295 | g >>> 11);
            g = b + (e ^ (c | ~f)) + d[12] + 1700485571 & 4294967295;
            b = c +
                (g << 6 & 4294967295 | g >>> 26);
            g = f + (c ^ (b | ~e)) + d[3] + 2399980690 & 4294967295;
            f = b + (g << 10 & 4294967295 | g >>> 22);
            g = e + (b ^ (f | ~c)) + d[10] + 4293915773 & 4294967295;
            e = f + (g << 15 & 4294967295 | g >>> 17);
            g = c + (f ^ (e | ~b)) + d[1] + 2240044497 & 4294967295;
            c = e + (g << 21 & 4294967295 | g >>> 11);
            g = b + (e ^ (c | ~f)) + d[8] + 1873313359 & 4294967295;
            b = c + (g << 6 & 4294967295 | g >>> 26);
            g = f + (c ^ (b | ~e)) + d[15] + 4264355552 & 4294967295;
            f = b + (g << 10 & 4294967295 | g >>> 22);
            g = e + (b ^ (f | ~c)) + d[6] + 2734768916 & 4294967295;
            e = f + (g << 15 & 4294967295 | g >>> 17);
            g = c + (f ^ (e | ~b)) + d[13] + 1309151649 & 4294967295;
            c = e + (g << 21 & 4294967295 | g >>> 11);
            g = b + (e ^ (c | ~f)) + d[4] + 4149444226 & 4294967295;
            b = c + (g << 6 & 4294967295 | g >>> 26);
            g = f + (c ^ (b | ~e)) + d[11] + 3174756917 & 4294967295;
            f = b + (g << 10 & 4294967295 | g >>> 22);
            g = e + (b ^ (f | ~c)) + d[2] + 718787259 & 4294967295;
            e = f + (g << 15 & 4294967295 | g >>> 17);
            g = c + (f ^ (e | ~b)) + d[9] + 3951481745 & 4294967295;
            a.g[0] = a.g[0] + b & 4294967295;
            a.g[1] = a.g[1] + (e + (g << 21 & 4294967295 | g >>> 11)) & 4294967295;
            a.g[2] = a.g[2] + e & 4294967295;
            a.g[3] = a.g[3] + f & 4294967295
        },
        rm = function(a, b) {
            if (!p(c)) var c = b.length;
            for (var d = c - a.h, e = a.v, f = a.l, g = 0; g < c;) {
                if (0 ==
                    f)
                    for (; g <= d;) qm(a, b, g), g += a.h;
                if (q(b))
                    for (; g < c;) {
                        if (e[f++] = b.charCodeAt(g++), f == a.h) {
                            qm(a, e);
                            f = 0;
                            break
                        }
                    } else
                        for (; g < c;)
                            if (e[f++] = b[g++], f == a.h) {
                                qm(a, e);
                                f = 0;
                                break
                            }
            }
            a.l = f;
            a.o += c
        };
    var sm = function() {
        this.h = null
    };
    ia(sm, Al);
    sm.prototype.g = function(a) {
        var b = Al.prototype.g.call(this, a);
        var c = fm = x();
        var d = jm(5);
        c = (hm ? !d : d) ? c | 2 : c & -3;
        d = jm(2);
        c = (im ? !d : d) ? c | 8 : c & -9;
        c = {
            s1: (c >>> 0).toString(16)
        };
        this.h || (this.h = om());
        b.v = this.h;
        b.A = zl(a, pl, c, "h", tm("kArwaWEsTs"));
        b.o = zl(a, rl, {}, "h", tm("b96YPMzfnx"));
        b.h = zl(a, sl, {}, "h", tm("yb8Wev6QDg"));
        return b
    };
    var tm = function(a) {
        return function(b) {
            var c = new pm;
            rm(c, b + a);
            var d = Array((56 > c.l ? c.h : 2 * c.h) - c.l);
            d[0] = 128;
            for (b = 1; b < d.length - 8; ++b) d[b] = 0;
            var e = 8 * c.o;
            for (b = d.length - 8; b < d.length; ++b) d[b] = e & 255, e /= 256;
            rm(c, d);
            d = Array(16);
            for (b = e = 0; 4 > b; ++b)
                for (var f = 0; 32 > f; f += 8) d[e++] = c.g[b] >>> f & 255;
            return ie(d).slice(-8)
        }
    };
    var um = function(a, b) {
            this.h = a;
            this.l = b
        },
        ak = function(a, b, c) {
            var d = a.g(c);
            if (v(d)) {
                var e = {};
                e = (e.sv = "654", e.cb = "j", e.e = vm(b), e);
                var f = sk(c, b, Vk(P));
                eb(e, f);
                c.bd[b] = f;
                a = 2 == c.sa() ? mi(e).join("&") : a.l.g(e).g;
                try {
                    return d(c.ia, a, b), 0
                } catch (g) {
                    return 2
                }
            } else return 1
        },
        vm = function(a) {
            var b = vl(a) ? "custom_metric_viewable" : a;
            a = $a(function(a) {
                return a == b
            });
            return Kj[a]
        };
    um.prototype.g = function() {
        return Ca(this.h)
    };
    var wm = function(a, b, c) {
        um.call(this, a, b);
        this.o = c
    };
    ia(wm, um);
    wm.prototype.g = function(a) {
        if (!a.Ma) return um.prototype.g.call(this, a);
        var b = this.o[a.Ma];
        if (b) return function(a, d, e) {
            xm(b, a, d, e)
        };
        Ch(393, Error());
        return null
    };
    var ym = function(a, b) {
            this.g = a;
            this.depth = b
        },
        Am = function(a) {
            a = a || Rg();
            var b = Math.max(a.length - 1, 0),
                c = Ug(a);
            a = c.g;
            var d = c.h,
                e = c.l,
                f = [];
            c = function(a, b) {
                return null == a ? b : a
            };
            e && f.push(new ym([e.url, e.oc ? 2 : 0], c(e.depth, 1)));
            d && d != e && f.push(new ym([d.url, 2], 0));
            a.url && a != e && f.push(new ym([a.url, 0], c(a.depth, b)));
            var g = kb(f, function(a, b) {
                return f.slice(0, f.length - b)
            });
            !a.url || (e || d) && a != e || (d = If(a.url)) && g.push([new ym([d, 1], c(a.depth, b))]);
            g.push([]);
            return kb(g, function(a) {
                return zm(b, a)
            })
        };

    function zm(a, b) {
        var c = lb(b, function(a, b) {
                return Math.max(a, b.depth)
            }, -1),
            d = Bb(c + 2);
        d[0] = a;
        z(b, function(a) {
            return d[a.depth + 1] = a.g
        });
        return d
    }
    var Bm = function() {
        var a = Am();
        return kb(a, function(a) {
            return Xg(a)
        })
    };
    var R = function() {
        Jl.call(this);
        this.F = void 0;
        this.G = null;
        this.D = !1;
        this.v = {};
        this.K = 0;
        this.o = new sm
    };
    ia(R, Jl);
    R.prototype.B = function(a, b) {
        var c = this;
        switch (N.C().R) {
            case "nis":
                var d = Cm(this, a, b);
                break;
            case "gsv":
                d = Dm(this, a, b);
                break;
            case "exc":
                d = Em(this, a);
                break;
            default:
                b.opt_overlayAdElement ? d = void 0 : b.opt_adElement ? d = Wl(this, a, b.opt_adElement, b.opt_osdId) : d = Zk(a) || void 0
        }
        d && 1 == d.sa() && (d.ca == Da && (d.ca = function(a) {
            return c.cd(a)
        }), Fm(this, d, b));
        return d
    };
    var Fm = function(a, b, c) {
        c = c.opt_configurable_tracking_events;
        if (null != a.g && Ga(c)) {
            var d = a.g;
            eg(c);
            z(c, function(a) {
                var c = kb(a.yi, function(a) {
                    var b = bm(a);
                    if (null == b) a = null;
                    else if (a = new am, null != b.visible && (a.g = b.visible / 100), null != b.audible && (a.h = 1 == b.audible), null != b.time) {
                        var c = "mtos" == b.timetype ? "mtos" : "tos",
                            d = Cb(b.time, "%") ? "%" : "ms";
                        b = parseInt(b.time, 10);
                        "%" == d && (b /= 100);
                        "ms" == d ? (a.l = b, a.o = -1) : (a.l = -1, a.o = b);
                        a.v = void 0 === c ? "tos" : c
                    }
                    return a
                });
                nb(c, function(a) {
                    return null == a
                }) || lk(b, new dm(a.id, a.event,
                    c, d))
            })
        }
    };
    R.prototype.cd = function(a) {
        var b = N.C();
        a.g = 0;
        a.I = 0;
        if ("h" == a.o || "n" == a.o) {
            if ("exc" == b.R || "nis" == b.R) var c = Ca("ima.bridge.getVideoMetadata");
            else if (a.Ma && Gm(this)) {
                var d = this.v[a.Ma];
                d ? c = function(a) {
                    Hm(d, a)
                } : null !== d && Ch("lidar::missingPlayerCallback", Error())
            } else c = Ca("ima.common.getVideoMetadata");
            if (v(c)) try {
                var e = c(a.ia)
            } catch (f) {
                a.g |= 4
            } else a.g |= 2
        } else if ("b" == a.o)
            if (b = Ca("ytads.bulleit.getVideoMetadata"), v(b)) try {
                e = b(a.ia)
            } catch (f) {
                a.g |= 4
            } else a.g |= 2;
            else if ("ml" == a.o)
            if (b = Ca("ima.common.getVideoMetadata"), v(b)) try {
                e =
                    b(a.ia)
            } catch (f) {
                a.g |= 4
            } else a.g |= 2;
            else a.g |= 1;
        a.g || (p(e) ? null === e ? a.g |= 16 : ab(e) ? a.g |= 32 : null != e.errorCode && (a.I = e.errorCode, a.g |= 64) : a.g |= 8);
        null != e || (e = {});
        Yl(e, a);
        Oh(e.volume) && Oh(this.F) && (e.volume *= this.F);
        return e
    };
    var Dm = function(a, b, c) {
            var d = Zk(b);
            d || (d = c.opt_nativeTime || -1, d = Ll(a, b, Pl(a), d), c.opt_osdId && (d.Ma = c.opt_osdId));
            return d
        },
        Cm = function(a, b, c) {
            var d = Zk(b);
            d || (d = Ll(a, b, "n", c.opt_nativeTime || -1), d.pa = N.C().D);
            return d
        },
        Em = function(a, b) {
            var c = Zk(b);
            c || (c = Ll(a, b, "h", -1));
            return c
        };
    R.prototype.Kc = function() {
        if (Gm(this)) return new wm("ima.common.triggerExternalActivityEvent", this.o, this.v);
        var a = Im(this);
        return null != a ? new um(a, this.o) : null
    };
    var Im = function(a) {
        var b = N.C();
        switch (Pl(a)) {
            case "b":
                return "ytads.bulleit.triggerExternalActivityEvent";
            case "n":
                return "ima.bridge.triggerExternalActivityEvent";
            case "h":
                if ("exc" == b.R) return "ima.bridge.triggerExternalActivityEvent";
            case "m":
            case "ml":
                return "ima.common.triggerExternalActivityEvent"
        }
        return null
    };
    R.prototype.H = function() {
        var a = this.g,
            b = Jl.prototype.H.call(this);
        b.push(new em(a));
        return b
    };
    R.prototype.yc = function(a) {
        !a.g && a.jb && Ql(this, a, "overlay_unmeasurable_impression") && (a.g = !0)
    };
    R.prototype.ld = function(a) {
        a.sd && (a.Xa() ? Ql(this, a, "overlay_viewable_end_of_session_impression") : Ql(this, a, "overlay_unviewable_impression"), a.sd = !1)
    };
    var Jm = function(a, b, c, d) {
        c = void 0 === c ? {} : c;
        var e = {};
        eb(e, {
            opt_adElement: void 0,
            opt_fullscreen: void 0
        }, c);
        if (e.opt_bounds) return a.o.g(ul("ol", d));
        if (p(d))
            if (c = tl(d), p(c))
                if (gl) b = ul("ue", d);
                else if (b = a.B(b, e)) {
            b: {
                Ol(a);
                "i" == fl && (b.jb = !0, a.zc(b));c = e.opt_fullscreen;p(c) && (b.Ca = !!c);dl() ? c = !1 : (c = N.C().R, c = J && J.zi || "nis" === c || "gsv" === c ? !1 : 0 === Ih(wf));
                var f = c;
                if (f) {
                    switch (b.sa()) {
                        case 1:
                            Rl(a, b, "pv");
                            break;
                        case 2:
                            a.yc(b)
                    }
                    P.o.cancel();
                    fl = "pv";
                    P.done = !0
                }
                c = d.toLowerCase();!f && rb(Hj, c) && Tl(a, b, e);0 != b.$ && rb(Ij,
                    c) && (b.jb || a.h || b.Bc());
                (f = b.$a[c]) && Qj(b.ba, f);
                switch (b.sa()) {
                    case 1:
                        var g = vl(c) ? a.I.custom_metric_viewable : a.I[c];
                        break;
                    case 2:
                        g = a.P[c]
                }
                if (g && (d = g.call(a, b, e, d), p(d))) {
                    e = ul(void 0, c);
                    eb(e, d);
                    d = e;
                    break b
                }
                d = void 0
            }
            3 == b.$ && a.A(b);b = d
        }
        else b = ul("nf", d);
        else b = void 0;
        else gl ? b = ul("ue") : (b = a.B(b, e)) ? (d = ul(), eb(d, rk(b, !0, !1, !1)), b = d) : b = ul("nf");
        return a.o.g(b)
    };
    R.prototype.J = function(a) {
        this.h && 1 == a.sa() && Km(this, a)
    };
    R.prototype.zc = function(a) {
        this.h && 1 == a.sa() && Km(this, a)
    };
    var Km = function(a, b) {
            var c;
            if (b.Ma && Gm(a)) {
                var d = a.v[b.Ma];
                d ? c = function(a, b) {
                    Lm(d, a, b)
                } : null !== d && Ch("lidar::missingPlayerCallback", Error())
            } else c = Ca("ima.common.triggerViewabilityMeasurementUpdate");
            if (v(c)) {
                var e = nk(b);
                e.nativeVolume = a.F;
                c(b.ia, e)
            }
        },
        yn = function(a, b, c) {
            a.v[b] = c
        },
        Gm = function(a) {
            return "exc" == N.C().R || "h" != Pl(a) && "m" != Pl(a) ? !1 : 0 != a.K
        };
    R.prototype.w = function(a, b, c, d) {
        a = Jl.prototype.w.call(this, a, b, c, d);
        this.D && (b = this.G, null == a.A && (a.A = new Lj), b.g[a.ia] = a.A, a.A.v = tk);
        return a
    };
    R.prototype.A = function(a) {
        a && 1 == a.sa() && this.D && delete this.G.g[a.ia];
        return Jl.prototype.A.call(this, a)
    };
    var zn = function(a) {
            var b = {};
            return b.viewability = a.g, b.googleViewability = a.l, b.moatInit = a.v, b.moatViewability = a.A, b.integralAdsViewability = a.o, b.doubleVerifyViewability = a.h, b
        },
        An = function(a, b, c) {
            c = void 0 === c ? {} : c;
            a = Jm(R.C(), b, c, a);
            return zn(a)
        };
    Ea(R);
    u("Goog_AdSense_Lidar_sendVastEvent", Bh(193, An, void 0, Ul), void 0);
    u("Goog_AdSense_Lidar_getViewability", Bh(194, function(a, b) {
        b = void 0 === b ? {} : b;
        a = Jm(R.C(), a, b);
        return zn(a)
    }), void 0);
    u("Goog_AdSense_Lidar_getUrlSignalsArray", Bh(195, function() {
        return Bm()
    }), void 0);
    u("Goog_AdSense_Lidar_getUrlSignalsList", Bh(196, function() {
        return Ae(Bm())
    }), void 0);
    var Bn = function() {
        var a = cg();
        if (a == a.top) return 0;
        for (; a && a != a.top && Df(a); a = a.parent) {
            if (a.sf_) return 2;
            if (a.$sf) return 3;
            if (a.inGptIF) return 4;
            if (a.inDapIF) return 5
        }
        return 1
    };
    var Cn = function(a, b) {
        this.h = {};
        this.g = [];
        this.o = this.l = 0;
        var c = arguments.length;
        if (1 < c) {
            if (c % 2) throw Error("Uneven number of arguments");
            for (var d = 0; d < c; d += 2) this.set(arguments[d], arguments[d + 1])
        } else if (a)
            if (a instanceof Cn)
                for (c = a.Ra(), d = 0; d < c.length; d++) this.set(c[d], a.get(c[d]));
            else
                for (d in a) this.set(d, a[d])
    };
    h = Cn.prototype;
    h.Ga = function() {
        return this.l
    };
    h.oa = function() {
        Dn(this);
        for (var a = [], b = 0; b < this.g.length; b++) a.push(this.h[this.g[b]]);
        return a
    };
    h.Ra = function() {
        Dn(this);
        return this.g.concat()
    };
    h.isEmpty = function() {
        return 0 == this.l
    };
    h.clear = function() {
        this.h = {};
        this.o = this.l = this.g.length = 0
    };
    var Dn = function(a) {
        if (a.l != a.g.length) {
            for (var b = 0, c = 0; b < a.g.length;) {
                var d = a.g[b];
                En(a.h, d) && (a.g[c++] = d);
                b++
            }
            a.g.length = c
        }
        if (a.l != a.g.length) {
            var e = {};
            for (c = b = 0; b < a.g.length;) d = a.g[b], En(e, d) || (a.g[c++] = d, e[d] = 1), b++;
            a.g.length = c
        }
    };
    h = Cn.prototype;
    h.get = function(a, b) {
        return En(this.h, a) ? this.h[a] : b
    };
    h.set = function(a, b) {
        En(this.h, a) || (this.l++, this.g.push(a), this.o++);
        this.h[a] = b
    };
    h.forEach = function(a, b) {
        for (var c = this.Ra(), d = 0; d < c.length; d++) {
            var e = c[d],
                f = this.get(e);
            a.call(b, f, e, this)
        }
    };
    h.clone = function() {
        return new Cn(this)
    };
    h.qb = function(a) {
        Dn(this);
        var b = 0,
            c = this.o,
            d = this,
            e = new qj;
        e.next = function() {
            if (c != d.o) throw Error("The map has changed since the iterator was created");
            if (b >= d.g.length) throw pj;
            var e = d.g[b++];
            return a ? e : d.h[e]
        };
        return e
    };
    var En = function(a, b) {
        return Object.prototype.hasOwnProperty.call(a, b)
    };
    var Fn = function(a, b) {
        this.g = this.w = this.o = "";
        this.B = null;
        this.v = this.l = "";
        this.A = !1;
        var c;
        a instanceof Fn ? (this.A = p(b) ? b : a.A, Gn(this, a.o), this.w = a.w, this.g = a.g, Hn(this, a.B), this.l = a.l, In(this, a.h.clone()), this.v = a.v) : a && (c = String(a).match(He)) ? (this.A = !!b, Gn(this, c[1] || "", !0), this.w = Jn(c[2] || ""), this.g = Jn(c[3] || "", !0), Hn(this, c[4]), this.l = Jn(c[5] || "", !0), In(this, c[6] || "", !0), this.v = Jn(c[7] || "")) : (this.A = !!b, this.h = new Kn(null, this.A))
    };
    Fn.prototype.toString = function() {
        var a = [],
            b = this.o;
        b && a.push(Ln(b, Mn, !0), ":");
        var c = this.g;
        if (c || "file" == b) a.push("//"), (b = this.w) && a.push(Ln(b, Mn, !0), "@"), a.push(encodeURIComponent(String(c)).replace(/%25([0-9a-fA-F]{2})/g, "%$1")), c = this.B, null != c && a.push(":", String(c));
        if (c = this.l) this.g && "/" != c.charAt(0) && a.push("/"), a.push(Ln(c, "/" == c.charAt(0) ? Nn : On, !0));
        (c = this.h.toString()) && a.push("?", c);
        (c = this.v) && a.push("#", Ln(c, Pn));
        return a.join("")
    };
    Fn.prototype.resolve = function(a) {
        var b = this.clone(),
            c = !!a.o;
        c ? Gn(b, a.o) : c = !!a.w;
        c ? b.w = a.w : c = !!a.g;
        c ? b.g = a.g : c = null != a.B;
        var d = a.l;
        if (c) Hn(b, a.B);
        else if (c = !!a.l) {
            if ("/" != d.charAt(0))
                if (this.g && !this.l) d = "/" + d;
                else {
                    var e = b.l.lastIndexOf("/"); - 1 != e && (d = b.l.substr(0, e + 1) + d)
                }
            e = d;
            if (".." == e || "." == e) d = "";
            else if (-1 != e.indexOf("./") || -1 != e.indexOf("/.")) {
                d = 0 == e.lastIndexOf("/", 0);
                e = e.split("/");
                for (var f = [], g = 0; g < e.length;) {
                    var k = e[g++];
                    "." == k ? d && g == e.length && f.push("") : ".." == k ? ((1 < f.length || 1 == f.length &&
                        "" != f[0]) && f.pop(), d && g == e.length && f.push("")) : (f.push(k), d = !0)
                }
                d = f.join("/")
            } else d = e
        }
        c ? b.l = d : c = "" !== a.h.toString();
        c ? In(b, a.h.clone()) : c = !!a.v;
        c && (b.v = a.v);
        return b
    };
    Fn.prototype.clone = function() {
        return new Fn(this)
    };
    var Gn = function(a, b, c) {
            a.o = c ? Jn(b, !0) : b;
            a.o && (a.o = a.o.replace(/:$/, ""))
        },
        Hn = function(a, b) {
            if (b) {
                b = Number(b);
                if (isNaN(b) || 0 > b) throw Error("Bad port number " + b);
                a.B = b
            } else a.B = null
        },
        In = function(a, b, c) {
            b instanceof Kn ? (a.h = b, Qn(a.h, a.A)) : (c || (b = Ln(b, Rn)), a.h = new Kn(b, a.A))
        },
        Jn = function(a, b) {
            return a ? b ? decodeURI(a.replace(/%25/g, "%2525")) : decodeURIComponent(a) : ""
        },
        Ln = function(a, b, c) {
            return q(a) ? (a = encodeURI(a).replace(b, Sn), c && (a = a.replace(/%25([0-9a-fA-F]{2})/g, "%$1")), a) : null
        },
        Sn = function(a) {
            a = a.charCodeAt(0);
            return "%" + (a >> 4 & 15).toString(16) + (a & 15).toString(16)
        },
        Mn = /[#\/\?@]/g,
        On = /[#\?:]/g,
        Nn = /[#\?]/g,
        Rn = /[#\?@]/g,
        Pn = /#/g,
        Kn = function(a, b) {
            this.h = this.g = null;
            this.l = a || null;
            this.o = !!b
        },
        Tn = function(a) {
            a.g || (a.g = new Cn, a.h = 0, a.l && Ie(a.l, function(b, c) {
                a.add(Eb(b), c)
            }))
        };
    Kn.prototype.Ga = function() {
        Tn(this);
        return this.h
    };
    Kn.prototype.add = function(a, b) {
        Tn(this);
        this.l = null;
        a = Un(this, a);
        var c = this.g.get(a);
        c || this.g.set(a, c = []);
        c.push(b);
        this.h += 1;
        return this
    };
    var Vn = function(a, b) {
        Tn(a);
        b = Un(a, b);
        En(a.g.h, b) && (a.l = null, a.h -= a.g.get(b).length, a = a.g, En(a.h, b) && (delete a.h[b], a.l--, a.o++, a.g.length > 2 * a.l && Dn(a)))
    };
    Kn.prototype.clear = function() {
        this.g = this.l = null;
        this.h = 0
    };
    Kn.prototype.isEmpty = function() {
        Tn(this);
        return 0 == this.h
    };
    var Wn = function(a, b) {
        Tn(a);
        b = Un(a, b);
        return En(a.g.h, b)
    };
    h = Kn.prototype;
    h.forEach = function(a, b) {
        Tn(this);
        this.g.forEach(function(c, d) {
            z(c, function(c) {
                a.call(b, c, d, this)
            }, this)
        }, this)
    };
    h.Ra = function() {
        Tn(this);
        for (var a = this.g.oa(), b = this.g.Ra(), c = [], d = 0; d < b.length; d++)
            for (var e = a[d], f = 0; f < e.length; f++) c.push(b[d]);
        return c
    };
    h.oa = function(a) {
        Tn(this);
        var b = [];
        if (q(a)) Wn(this, a) && (b = wb(b, this.g.get(Un(this, a))));
        else {
            a = this.g.oa();
            for (var c = 0; c < a.length; c++) b = wb(b, a[c])
        }
        return b
    };
    h.set = function(a, b) {
        Tn(this);
        this.l = null;
        a = Un(this, a);
        Wn(this, a) && (this.h -= this.g.get(a).length);
        this.g.set(a, [b]);
        this.h += 1;
        return this
    };
    h.get = function(a, b) {
        if (!a) return b;
        a = this.oa(a);
        return 0 < a.length ? String(a[0]) : b
    };
    h.toString = function() {
        if (this.l) return this.l;
        if (!this.g) return "";
        for (var a = [], b = this.g.Ra(), c = 0; c < b.length; c++) {
            var d = b[c],
                e = encodeURIComponent(String(d));
            d = this.oa(d);
            for (var f = 0; f < d.length; f++) {
                var g = e;
                "" !== d[f] && (g += "=" + encodeURIComponent(String(d[f])));
                a.push(g)
            }
        }
        return this.l = a.join("&")
    };
    h.clone = function() {
        var a = new Kn;
        a.l = this.l;
        this.g && (a.g = this.g.clone(), a.h = this.h);
        return a
    };
    var Un = function(a, b) {
            b = String(b);
            a.o && (b = b.toLowerCase());
            return b
        },
        Qn = function(a, b) {
            b && !a.o && (Tn(a), a.l = null, a.g.forEach(function(a, b) {
                var c = b.toLowerCase();
                b != c && (Vn(this, b), Vn(this, c), 0 < a.length && (this.l = null, this.g.set(Un(this, c), xb(a)), this.h += a.length))
            }, a));
            a.o = b
        };
    var Xn = "://secure-...imrworldwide.com/ ://cdn.imrworldwide.com/ ://aksecure.imrworldwide.com/ ://[^.]*.moatads.com ://youtube[0-9]+.moatpixel.com ://pm.adsafeprotected.com/youtube ://pm.test-adsafeprotected.com/youtube ://e[0-9]+.yt.srs.doubleverify.com www.google.com/pagead/sul www.google.com/pagead/xsul www.youtube.com/pagead/sul www.youtube.com/pagead/psul www.youtube.com/pagead/slav".split(" "),
        Yn = /\bocr\b/,
        Zn = 0,
        $n = {},
        ao = function(a) {
            if (A(Qb(a))) return !1;
            if (0 <= a.indexOf("://pagead2.googlesyndication.com/pagead/gen_204?id=yt3p&sr=1&")) return !0;
            try {
                var b = new Fn(a)
            } catch (c) {
                return null != pb(Xn, function(b) {
                    return 0 < a.search(b)
                })
            }
            return b.v.match(Yn) ? !0 : null != pb(Xn, function(b) {
                return null != a.match(b)
            })
        },
        fo = function(a, b) {
            if (a && (a = bo(a), !A(a))) {
                var c = 'javascript:"<body><img src=\\""+' + a + '+"\\"></body>"';
                b ? co(function(b) {
                    eo(b ? c : 'javascript:"<body><object data=\\""+' + a + '+"\\" type=\\"text/html\\" width=1 height=1 style=\\"visibility:hidden;\\"></body>"')
                }) : eo(c)
            }
        },
        eo = function(a) {
            var b = ld("IFRAME", {
                src: a,
                style: "display:none"
            });
            a = bd(b).body;
            var c =
                fe(function() {
                    Yd(d);
                    nd(b)
                }, 15E3);
            var d = Od(b, ["load", "error"], function() {
                fe(function() {
                    n.clearTimeout(c);
                    nd(b)
                }, 5E3)
            });
            a.appendChild(b)
        },
        co = function(a) {
            var b = $n.imageLoadingEnabled;
            if (null != b) a(b);
            else {
                var c = !1;
                go(function(b, e) {
                    delete $n[e];
                    c || (c = !0, null != $n.imageLoadingEnabled || ($n.imageLoadingEnabled = b), a(b))
                })
            }
        },
        go = function(a) {
            var b = new Image,
                c = "" + Zn++;
            $n[c] = b;
            b.onload = function() {
                clearTimeout(d);
                a(!0, c)
            };
            var d = setTimeout(function() {
                a(!1, c)
            }, 300);
            b.src = "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="
        },
        ho = function(a) {
            if (a) {
                var b = document.createElement("OBJECT");
                b.data = a;
                b.width = "1";
                b.height = "1";
                b.style.visibility = "hidden";
                var c = "" + Zn++;
                $n[c] = b;
                b.onload = b.onerror = function() {
                    delete $n[c]
                };
                document.body.appendChild(b)
            }
        },
        io = function(a) {
            if (a) {
                var b = new Image,
                    c = "" + Zn++;
                $n[c] = b;
                b.onload = b.onerror = function() {
                    delete $n[c]
                };
                b.src = a
            }
        },
        jo = function(a, b) {
            a && (b ? co(function(b) {
                b ? io(a) : ho(a)
            }) : io(a))
        },
        bo = function(a) {
            a instanceof Rc || (a = a.Va ? a.Ha() : String(a), Tc.test(a) || (a = "about:invalid#zClosurez"), a = Uc(a));
            var b =
                Sc(a);
            if ("about:invalid#zClosurez" === b) return "";
            b instanceof Wc ? a = b : (a = null, b.hc && (a = b.Zb()), b = Mb(b.Va ? b.Ha() : String(b)), a = Xc(b, a));
            a instanceof Wc && a.constructor === Wc && a.l === Vc ? a = a.g : (Fa(a), a = "type_error:SafeHtml");
            return encodeURIComponent(String(Ae(a)))
        };
    var ko = /(?:\[|%5B)([a-zA-Z0-9_]+)(?:\]|%5D)/g,
        lo = function(a, b) {
            return a.replace(ko, function(a, d) {
                try {
                    var c = bb(b, d);
                    if (null == c) return a;
                    c = c.toString();
                    if ("" == c || !A(Qb(c))) return encodeURIComponent(c).replace(/%2C/g, ",")
                } catch (f) {}
                return a
            })
        };
    var mo = "ad_type vpos mridx pos vad_type videoad_start_delay".split(" ");

    function no() {
        Ge.set("GoogleAdServingTest", "Good");
        var a = "Good" == Ge.get("GoogleAdServingTest");
        a && (Ge.get("GoogleAdServingTest"), Ge.set("GoogleAdServingTest", "", 0, void 0, void 0));
        return a
    };
    var oo = ["*.youtu.be", "*.youtube.com"],
        po = "ad.doubleclick.net bid.g.doubleclick.net ggpht.com google.co.uk google.com googleads.g.doubleclick.net googleads4.g.doubleclick.net googleadservices.com googlesyndication.com googleusercontent.com gstatic.com gvt1.com prod.google.com pubads.g.doubleclick.net s0.2mdn.net static.doubleclick.net surveys.g.doubleclick.net youtube.com ytimg.com".split(" "),
        qo = ["c.googlesyndication.com"];

    function ro(a, b) {
        return (new RegExp("^https?://([a-z0-9-]{1,63}\\.)*(" + b.join("|").replace(/\./g, "\\.") + ")(:[0-9]+)?([/?#]|$)", "i")).test(a)
    }
    var to = function(a, b) {
        try {
            var c = (new Fn(b)).g;
            c = c.replace(/^www./i, "");
            return a.some(function(a) {
                return so(a, c)
            })
        } catch (d) {
            return !1
        }
    };

    function so(a, b) {
        if (A(Qb(b))) return !1;
        a = a.toLowerCase();
        b = b.toLowerCase();
        return "*." == a.substr(0, 2) ? (a = a.substr(2), a.length > b.length ? !1 : b.substr(-a.length) == a && (b.length == a.length || "." == b.charAt(b.length - a.length - 1))) : a == b
    };
    var uo = function(a, b, c) {
            Ef(b, function(b, e) {
                !b && 0 !== b || c[e] || (a += "&" + encodeURIComponent(e) + "=" + encodeURIComponent(String(b)), c[e] = !0)
            });
            return a
        },
        Ao = function(a, b, c, d, e, f, g, k) {
            f = void 0 === f ? Infinity : f;
            g = void 0 === g ? !1 : g;
            nh.call(this, a, k);
            var m = this;
            this.H = 0;
            this.D = f;
            this.P = b;
            this.F = c;
            this.O = d;
            this.U = e;
            this.K = !("csi.gstatic.com" !== this.F || !this.g.navigator || !this.g.navigator.sendBeacon);
            this.w = {};
            this.g.performance && this.g.performance.now || vo(this, "dat", 1);
            this.g.navigator && this.g.navigator.deviceMemory &&
                vo(this, "dmc", this.g.navigator.deviceMemory);
            this.I = !g;
            this.G = function() {
                m.g.setTimeout(function() {
                    return wo(m)
                }, 1100)
            };
            this.V = [];
            this.L = function() {
                z(m.V, function(a) {
                    try {
                        a()
                    } catch (t) {}
                });
                wo(m)
            };
            this.J = this.g.setTimeout(function() {
                return wo(m)
            }, 5E3);
            this.l = {};
            this.A = b.length + c.length + d.length + e.length + 3;
            this.o = 0;
            z(this.events, function(a) {
                return xo(m, a)
            });
            yo(this);
            zo(this)
        };
    ia(Ao, nh);
    var zo = function(a) {
            "complete" === a.g.document.readyState ? a.g.setTimeout(function() {
                return wo(a)
            }, 0) : Wf(a.g, "load", a.G);
            Wf(a.g, "unload", a.L)
        },
        vo = function(a, b, c) {
            c = String(c);
            a.A = null != a.w[b] ? a.A + (c.length - a.w[b].length) : a.A + (b.length + c.length + 2);
            a.w[b] = c
        },
        Bo = function(a, b, c, d, e) {
            e = void 0 === e ? "" : e;
            var f = null == a.l[b] ? b.length + c.length + 2 : d ? c.length + e.length : c.length - a.l[b].length;
            8E3 < a.A + a.o + f && (wo(a), f = b.length + c.length + 2);
            a.l[b] = d && null != a.l[b] ? a.l[b] + ("" + e + c) : c;
            a.o += f;
            6E3 <= a.A + a.o && wo(a)
        },
        wo = function(a) {
            if (a.h &&
                a.I) {
                try {
                    if (a.o) {
                        var b = a.l;
                        a.H++;
                        var c = a.P + "//" + a.F + a.O + a.U,
                            d = {};
                        c = uo(c, a.w, d);
                        c = uo(c, b, d);
                        a.g.google_timing_params && (c = uo(c, a.g.google_timing_params, d), a.g.google_timing_params = void 0);
                        var e = c;
                        b = !1;
                        try {
                            b = a.K && a.g.navigator && a.g.navigator.sendBeacon(e, null)
                        } catch (f) {
                            a.K = !1
                        }
                        b || Yf(a.g, e, void 0);
                        yo(a);
                        a.H === a.D && a.v()
                    }
                } catch (f) {
                    (new vh).Ka(358, f)
                }
                a.l = {};
                a.o = 0;
                a.events.length = 0;
                a.g.clearTimeout(a.J);
                a.J = 0
            }
        },
        yo = function(a) {
            vo(a, "puid", (a.H + 1).toString(36) + "~" + x().toString(36))
        },
        xo = function(a, b) {
            var c =
                "met." + b.type,
                d = r(b.value) ? Math.round(b.value).toString(36) : b.value,
                e = Math.round(b.duration);
            b = "" + b.label + (null != b.slotId ? "_" + b.slotId : "") + ("." + d) + (0 < e ? "_" + e.toString(36) : "");
            Bo(a, c, b, !0, "~")
        };
    Ao.prototype.B = function(a) {
        this.h && this.H < this.D && (nh.prototype.B.call(this, a), xo(this, a))
    };
    Ao.prototype.v = function() {
        nh.prototype.v.call(this);
        this.g.clearTimeout(this.J);
        this.o = this.J = 0;
        this.l = {};
        Xf(this.g, "load", this.G);
        Xf(this.g, "unload", this.L)
    };
    var Do = function() {
            this.g = new Ao(1, "https:", "csi.gstatic.com", "/csi?v=2&s=", "ima", void 0, !0);
            Co(this, "c", Aj());
            this.h = "0"
        },
        Co = function(a, b, c) {
            null != c && vo(a.g, b, c)
        };
    Ea(Do);
    var ima = {};
    var Eo = function(a) {
        this.h = a
    };
    Eo.prototype.g = function() {
        return this.h
    };
    var Fo = function() {
        G.call(this);
        this.currentTime = 0
    };
    y(Fo, G);
    var Go = function(a, b, c) {
        this.l = b;
        this.h = c;
        this.o = a
    };
    y(Go, Error);
    h = Go.prototype;
    h.ie = function() {
        return this.g
    };
    h.je = function() {
        return this.l
    };
    h.he = function() {
        return this.h
    };
    h.Vd = function() {
        return 1E3 > this.h ? this.h : 900
    };
    h.ke = function() {
        return this.o
    };
    h.toString = function() {
        return "AdError " + this.h + ": " + this.l + (null != this.g ? " Caused by: " + this.g : "")
    };
    var Ho = function(a) {
        if (null == a.errorCode || null == a.errorMessage || null == a.type) return null;
        for (var b = new Go(a.type, a.errorMessage, a.errorCode), c = b, d = a.innerError, e = 0; 3 > e; ++e)
            if (d instanceof Object) {
                var f = new Go(d.type, d.errorMessage, d.errorCode);
                c = c.g = f;
                d = d.innerError
            } else {
                null != d && (c.g = Error(a.innerError));
                break
            }
        return b
    };
    var Io = function(a, b) {
        zd.call(this, "adError");
        this.h = a;
        this.o = b ? b : null
    };
    y(Io, zd);
    Io.prototype.v = function() {
        return this.h
    };
    Io.prototype.A = function() {
        return this.o
    };
    var S = function(a, b, c) {
        zd.call(this, a);
        this.v = b;
        this.o = null != c ? c : null
    };
    y(S, zd);
    S.prototype.w = function() {
        return this.v
    };
    S.prototype.B = function() {
        return this.o
    };
    var Jo = function(a) {
            this.g = a
        },
        Mo = function() {
            var a = Ko(T);
            return Lo(a, "disableExperiments")
        },
        Lo = function(a, b) {
            return Xa(a.g, b) && (a = a.g[b], ya(a)) ? a : !1
        },
        No = function(a) {
            if (Xa(a.g, "forceExperimentIds")) {
                a = a.g.forceExperimentIds;
                var b = [],
                    c = 0;
                Ga(a) && a.forEach(function(a) {
                    r(a) && (b[c++] = a)
                });
                return b
            }
            return null
        };
    var U = function() {
            this.J = "always";
            this.B = 4;
            this.I = 1;
            this.g = 0;
            this.l = !0;
            this.h = !1;
            this.w = "en";
            this.U = this.F = !1;
            this.H = this.A = "";
            this.G = null;
            this.X = this.O = -1;
            this.V = this.L = this.K = "";
            this.o = !1;
            this.v = !0;
            try {
                this.fa = Am(void 0)[0]
            } catch (a) {}
        },
        Oo = "af am ar bg bn ca cs da de el en en_gb es es_419 et eu fa fi fil fr fr_ca gl gu he hi hr hu id in is it iw ja kn ko lt lv ml mr ms nb nl no pl pt_br pt_pt ro ru sk sl sr sv sw ta te th tr uk ur vi zh_cn zh_hk zh_tw zu".split(" "),
        Po = function(a) {
            a = Qb(a);
            A(a) || (a =
                a.substring(0, 20));
            return a
        };
    h = U.prototype;
    h.pf = function(a) {
        this.J = a
    };
    h.cf = function() {
        return this.J
    };
    h.wf = function(a) {
        this.B = a
    };
    h.gf = function() {
        return this.B
    };
    h.zf = function(a) {
        this.P = a
    };
    h.kf = function() {
        return this.P
    };
    h.Bf = function(a) {
        ya(a) && (this.I = a ? 1 : 0)
    };
    h.Cf = function(a) {
        this.I = a
    };
    h.nf = function(a) {
        this.l = a
    };
    h.lf = function() {
        return this.l
    };
    h.eg = function() {
        return !1
    };
    h.dg = function() {
        return !1
    };
    h.Af = function(a) {
        this.h = a
    };
    h.Kf = function() {
        return this.h
    };
    h.cg = function() {
        return !0
    };
    h.Y = function() {
        return !1
    };
    h.lc = function() {
        return !1
    };
    h.Jf = function() {
        return !1
    };
    h.tf = function(a) {
        this.F = a
    };
    h.mf = function() {
        return this.F
    };
    h.uf = function(a) {
        this.U = a
    };
    h.Nb = function() {
        return this.U
    };
    h.kc = function() {
        return !1
    };
    h.$f = function() {
        return !1
    };
    h.vf = function(a) {
        if (null != a) {
            a = a.toLowerCase().replace("-", "_");
            if (!Oo.includes(a) && (a = (a = a.match(/^\w{2,3}([-_]|$)/)) ? a[0].replace(/[_-]/g, "") : "", !Oo.includes(a))) return;
            this.w = a
        }
    };
    h.Td = function() {
        return this.w
    };
    h.xf = function(a) {
        this.A = Po(a)
    };
    h.hf = function() {
        return this.A
    };
    h.yf = function(a) {
        this.H = Po(a)
    };
    h.jf = function() {
        return this.H
    };
    var Ko = function(a) {
        if (null == a.G) {
            var b = {},
                c = (new Fn(F().location.href)).h;
            if (Wn(c, "tcnfp")) try {
                b = JSON.parse(c.get("tcnfp"))
            } catch (d) {}
            a.G = new Jo(b)
        }
        return a.G
    };
    U.prototype.ca = function(a) {
        this.O = a
    };
    U.prototype.ea = function(a) {
        this.X = a
    };
    var So = function() {
            var a = T;
            Qo();
            a.K = Ro[1] || ""
        },
        To = function() {
            var a = T;
            Qo();
            a.V = Ro[6] || ""
        },
        Uo = function() {
            var a = T;
            Qo();
            a.L = Ro[4] || ""
        };
    h = U.prototype;
    h.rf = function(a) {
        this.o = a
    };
    h.df = function() {
        return this.o
    };
    h.qf = function(a) {
        this.v = a
    };
    h.sf = function() {};
    h.ff = function() {
        return !0
    };
    var T = new U;
    var Vo = {
        Dg: "application/dash+xml",
        ph: "video/mp4",
        rh: "video/mpeg",
        mh: "application/x-mpegURL",
        vh: "video/ogg",
        ei: "video/3gpp",
        ui: "video/webm",
        oh: "audio/mpeg",
        qh: "audio/mp4"
    };
    var Wo = function(a, b, c) {
        this.g = a;
        this.h = Math.min(Math.max(b || 0, 0), 1);
        this.l = null != c ? c : !0
    };
    var Xo = function(a) {
            this.l = a;
            this.h = new Cn;
            this.g = null
        },
        Yo = function(a) {
            var b = Math.random(),
                c = 0,
                d = a.h.oa();
            d.forEach(function(a) {
                c += a.h
            });
            var e = 1 < c ? c : 1;
            a.g = null;
            for (var f = 0, g = 0; g < d.length; ++g)
                if (f += d[g].h, f / e >= b) {
                    a.g = d[g];
                    break
                }
        };
    var ap = function() {
            this.h = null != n.G_testRunner;
            this.g = new Cn;
            W(this, "GvnExternalLayer", 31061774, .01);
            W(this, "GvnExternalLayer", 31061775, .01);
            W(this, "GvnExternalLayer", 605457E3, .05);
            W(this, "GvnExternalLayer", 605457001, .05);
            W(this, "GvnExternalLayer", 41341310, 0);
            W(this, "GvnExternalLayer", 41341311, 0);
            W(this, "GvnExternalLayer", 420706068, .01);
            W(this, "GvnExternalLayer", 420706069, .01);
            W(this, "GvnExternalLayer", 41351070, .01);
            W(this, "GvnExternalLayer", 41351071, .01);
            W(this, "ActiveViewExternalLayer", 668123010,
                .01);
            W(this, "ActiveViewExternalLayer", 668123011, .01);
            W(this, "ActiveViewExternalLayer", 668123008, .01);
            W(this, "ActiveViewExternalLayer", 668123009, .01);
            W(this, "ActiveViewExternalLayer", 668123028, .01);
            W(this, "ActiveViewExternalLayer", 668123029, .01);
            W(this, "ActiveViewExternalLayer", 953563515, .01);
            W(this, "ActiveViewExternalLayer", 953563516, .01);
            W(this, "ActiveViewExternalLayer", 953563517, .01);
            W(this, "GvnExternalLayer", 413051065, .01);
            W(this, "GvnExternalLayer", 413051066, .01);
            W(this, "GvnExternalLayer", 651800003,
                .01);
            W(this, "GvnExternalLayer", 651800004, .01);
            W(this, "GvnExternalLayer", 667080011, .01);
            W(this, "GvnExternalLayer", 667080012, .01);
            W(this, "GvnExternalLayer", 231422001, .01);
            W(this, "GvnExternalLayer", 231422002, .01);
            W(this, "GvnExternalLayer", 667080009, 0);
            W(this, "GvnExternalLayer", 667080010, 0);
            W(this, "GvnExternalLayer", 4081988, 0);
            W(this, "GvnExternalLayer", 4081989, .01);
            W(this, "GvnExternalLayer", 651800005, .01);
            W(this, "GvnExternalLayer", 651800006, .01);
            W(this, "GvnExternalLayer", 328840010, .05);
            W(this, "GvnExternalLayer",
                328840011, .05);
            W(this, "GvnExternalLayer", 231422003, .01);
            W(this, "GvnExternalLayer", 231422004, .01);
            W(this, "GvnExternalLayer", 1369610001, .05);
            W(this, "GvnExternalLayer", 40819880, .005);
            W(this, "GvnExternalLayer", 40819881, .005);
            if (!B(navigator.userAgent, "AppleTV")) {
                Zo(this);
                var a = Ko(T);
                a = No(a);
                null != a && (this.h = !1, $o(this, a.map(String)))
            }
        },
        bp = ["ActiveViewExternalLayer"],
        cp = null,
        dp = function() {
            cp || (cp = new ap);
            return cp
        },
        W = function(a, b, c, d) {
            A(Qb(b)) || isNaN(c) || 0 >= c || (c = new Wo(c, d), ep(a, b).h.set(c.g, c))
        },
        Zo =
        function(a) {
            Mo() || T.kc() || a.g.oa().forEach(function(a) {
                Yo(a)
            })
        },
        $o = function(a, b) {
            b.forEach(function(b) {
                var c = Number(b);
                b = "FORCED_PUB_EXP_LAYER_" + b;
                isNaN(c) || 0 >= c || A(Qb(b)) || (ep(a, b).g = new Wo(c, 0, !0))
            })
        },
        fp = function() {
            var a = {};
            dp().g.oa().forEach(function(b) {
                var c = b.g;
                if (c) {
                    var d = {};
                    d.experimentId = c.g;
                    d.shouldReport = c.l;
                    a[b.l] = d
                } else a[b.l] = {}
            });
            return a
        },
        gp = function(a, b) {
            return a.h ? !1 : a.g.oa().some(function(a) {
                return !!a.g && a.g.g == b
            })
        },
        hp = function() {
            var a = dp();
            if (a.h) return "";
            var b = [];
            a.g.oa().forEach(function(a) {
                (a =
                    a.g) && a.l && b.push(a.g)
            });
            return b.sort().join(",")
        },
        ep = function(a, b) {
            var c = a.g.get(b);
            null == c && (c = new Xo(b), a.g.set(b, c));
            return c
        },
        ip = function() {
            var a = [],
                b = dp();
            bp.forEach(function(c) {
                (c = (c = ep(b, c)) ? c.g : null) && a.push(c.g)
            });
            return a
        };
    var jp = "abort canplay canplaythrough durationchange emptied loadstart loadeddata loadedmetadata progress ratechange seeked seeking stalled suspend waiting".split(" ");
    var kp = function(a) {
        return (a = a.exec(C)) ? a[1] : ""
    };
    (function() {
        if (je) return kp(/Firefox\/([0-9.]+)/);
        if (pc || qc || oc) return Cc;
        if (te) return jc() || D("iPad") || D("iPod") ? kp(/CriOS\/([0-9.]+)/) : kp(/Chrome\/([0-9.]+)/);
        if (ue && !(jc() || D("iPad") || D("iPod"))) return kp(/Version\/([0-9.]+)/);
        if (le || re) {
            var a = /Version\/(\S+).*Mobile\/(\S+)/.exec(C);
            if (a) return a[1] + "." + a[2]
        } else if (se) return (a = kp(/Android\s+([0-9.]+)/)) ? a : kp(/Version\/([0-9.]+)/);
        return ""
    })();
    var lp = {},
        mp = "",
        np = /OS (\S+) like/,
        op = /Android ([\d\.]+)/,
        pp = function() {
            return gp(dp(), 605457001) || D("Macintosh") && gc() && 0 <= Sb(ic(), 11) || !Vh() && !Uh() && fc() && 0 <= Sb(ic(), 65) || !T.Y() && (Vh() || Uh()) ? !0 : !1
        },
        qp = function() {
            return uc && !vc || B(C, "iPod")
        },
        rp = function() {
            return qp() || vc
        },
        tp = function(a) {
            return rp() && sp(np, a)
        },
        sp = function(a, b) {
            null == lp[b] && (A(mp) && (a = a.exec(C)) && (mp = a[1]), (a = mp) ? (a = a.replace(/_/g, "."), lp[b] = 0 <= Sb(a, b)) : lp[b] = !1);
            return lp[b]
        },
        up = function() {
            var a = C;
            return a ? B(a, "Nintendo WiiU") : !1
        },
        vp = function() {
            return se || (T.Y() || !1) && tc && !(tc && sp(op, 4.4))
        },
        wp = function(a) {
            return T.Y() || T.Nb() && pp() || rp() && (!(vc || tp(10) && T.o) || !a) || tc && (!tc || !sp(op, 4)) || dl() ? !0 : !1
        };
    var xp = ["*.googlesyndication.com", "gcdn.2mdn.net"];
    var yp = function(a) {
        try {
            a: {
                var b = a,
                    c = void 0,
                    d = b.length - 11 - 2;
                if (!(-1 == b.indexOf("URL_SIGNALS") || 2048 <= d || !c && !window.Goog_AdSense_Lidar_getUrlSignalsArray)) {
                    c = c || window.Goog_AdSense_Lidar_getUrlSignalsArray();
                    d = {};
                    for (var e = 0; e < c.length; ++e) {
                        d.URL_SIGNALS = c[e];
                        var f = lo(b, d);
                        if (2048 > f.length) {
                            a = f;
                            break a
                        }
                    }
                }
                a = b
            }
        }
        catch (l) {}
        try {
            f = a;
            var g = B(C, "AppleTV") ? "https" : window.location.protocol;
            g = void 0 === g ? window.location.protocol : g;
            b = !1;
            ro(f, qo) ? b = !1 : (null == f ? 0 : to(oo, f)) ? b = !0 : "https:" == g && ro(f, po) && (b = !0);
            if (b) {
                var k =
                    new Fn(f);
                "https" == k.o ? a = f : (Gn(k, "https"), a = k.toString())
            } else a = f;
            var m = !T.Y();
            (g = a) && (ao(g) ? fo(g, m) : jo(g, m))
        } catch (l) {}
    };
    var zp = function(a, b) {
            this.message = a;
            this.g = b
        },
        Ap = new zp("Invalid usage of the API. Cause: {0}", 900),
        Bp = new zp("Failed to initialize ad playback element before starting ad playback.", 400),
        Cp = new zp("The provided {0} information: {1} is invalid.", 1101),
        Dp = function(a, b, c) {
            var d = b || null;
            if (!(d instanceof Go)) {
                var e = a.g,
                    f = a.message,
                    g = Array.prototype.slice.call(arguments, 2);
                if (0 < g.length)
                    for (var k = 0; k < g.length; k++) f = f.replace(new RegExp("\\{" + k + "\\}", "ig"), g[k]);
                e = new Go("adPlayError", f, e);
                e.g = d;
                d = e
            }
            return d
        };
    var Ep = function(a) {
        wd.call(this);
        this.o = a;
        this.h = {}
    };
    y(Ep, wd);
    var Fp = [];
    Ep.prototype.N = function(a, b, c, d) {
        return Gp(this, a, b, c, d)
    };
    var Gp = function(a, b, c, d, e, f) {
            Ga(c) || (c && (Fp[0] = c.toString()), c = Fp);
            for (var g = 0; g < c.length; g++) {
                var k = Pd(b, c[g], d || a.handleEvent, e || !1, f || a.o || a);
                if (!k) break;
                a.h[k.key] = k
            }
            return a
        },
        Hp = function(a, b, c, d, e, f) {
            if (Ga(c))
                for (var g = 0; g < c.length; g++) Hp(a, b, c[g], d, e, f);
            else(b = Od(b, c, d || a.handleEvent, e, f || a.o || a)) && (a.h[b.key] = b)
        };
    Ep.prototype.Na = function(a, b, c, d, e) {
        if (Ga(b))
            for (var f = 0; f < b.length; f++) this.Na(a, b[f], c, d, e);
        else c = c || this.handleEvent, d = Ia(d) ? !!d.capture : !!d, e = e || this.o || this, c = Qd(c), d = !!d, b = Dd(a) ? Kd(a.A, String(b), c, d, e) : a ? (a = Td(a)) ? Kd(a, b, c, d, e) : null : null, b && (Yd(b), delete this.h[b.key])
    };
    var Ip = function(a) {
        Pa(a.h, function(a, c) {
            this.h.hasOwnProperty(c) && Yd(a)
        }, a);
        a.h = {}
    };
    Ep.prototype.T = function() {
        Ep.da.T.call(this);
        Ip(this)
    };
    Ep.prototype.handleEvent = function() {
        throw Error("EventHandler.handleEvent not implemented");
    };
    var Jp = function(a) {
        Fo.call(this);
        this.currentTime = a.currentTime;
        if (!("currentTime" in a) || isNaN(a.currentTime)) throw Dp(Cp, null, "content", "currentTime");
        this.duration = "duration" in a && !isNaN(a.duration) ? a.duration : -1;
        this.h = a;
        this.g = new ee(250);
        this.l = new Ep(this);
        Gp(this.l, this.g, "tick", this.o, !1, this)
    };
    y(Jp, Fo);
    Jp.prototype.start = function() {
        this.g.start()
    };
    Jp.prototype.stop = function() {
        this.g.stop()
    };
    Jp.prototype.T = function() {
        Jp.da.T.call(this);
        this.l.W();
        this.g.W()
    };
    Jp.prototype.o = function() {
        if ("currentTime" in this.h && !isNaN(this.h.currentTime)) {
            var a = this.currentTime;
            this.currentTime = this.h.currentTime;
            a != this.currentTime && this.dispatchEvent(new zd("currentTimeUpdate"))
        } else this.dispatchEvent(new zd("contentWrapperError")), this.stop()
    };
    var Kp = function() {
        this.loadVideoTimeout = T.Y() ? 15E3 : 8E3
    };
    h = Kp.prototype;
    h.autoAlign = !0;
    h.baseYouTubeUrl = null;
    h.bitrate = -1;
    h.uiElements = null;
    h.contentId = null;
    h.disableClickThrough = !1;
    h.enablePreloading = !1;
    h.customPlayerSupportsPreloading = !1;
    h.mimeTypes = null;
    h.restoreCustomPlaybackStateOnAdBreakComplete = !1;
    h.useLearnMoreButton = !1;
    h.useMuteToggleButton = !1;
    h.useStyledLinearAds = !1;
    h.useStyledNonLinearAds = !1;
    h.playAdsAfterTime = -1;
    h.useVideoAdUi = !0;
    h.enableVideoTouchMove = !1;
    h.youTubeAdNamespace = 0;
    h.loadVideoTimeout = 8E3;
    h.disableUi = !1;
    var Lp = function() {
            this.l = -1;
            this.h = this.g = null
        },
        Mp = new Lp;
    Lp.prototype.clear = function() {
        this.h = this.g = null
    };
    var Np = function() {
        var a = "h.3.217.0";
        null != Mp.h ? (a += "/n." + Mp.h, null != Mp.g && (a += "/" + Mp.g)) : T.Y() && (a += "/o.0.0.0");
        return a
    };
    var Op = function() {
        this.h = .01 > Math.random();
        this.g = Math.floor(4503599627370496 * Math.random())
    };
    Ea(Op);
    var Sp = function(a, b, c, d) {
            if (null == n.G_testRunner && (a.h || d) && !B(C, "AppleTV")) {
                c = c || {};
                c.lid = b;
                c.sdkv = Np();
                b = hp();
                A(Qb(b)) || (c.e = b);
                c = Pp(a, c);
                var e = new Fn("http://pagead2.googlesyndication.com/pagead/gen_204");
                Pa(c, function(a, b) {
                    e.h.set(b, null == a ? "" : "boolean" == typeof a ? a ? "t" : "f" : "" + a)
                }, a);
                a = Qp();
                Gn(e, a.o);
                T.kc() || (a = e.toString(), b = e.h.get("url"), null != b && cc() && 2083 < a.length && (a = Rp(e, b)), yp(a))
            }
        },
        Rp = function(a, b) {
            a.h.set("url", "");
            try {
                var c = 2083 - a.toString().length - 1;
                if (0 >= c) return a.toString();
                for (var d =
                        b.slice(0, c), e = encodeURIComponent(d), f = c; 0 < f && e.length > c;) d = b.slice(0, f--), e = encodeURIComponent(d);
                a.h.set("url", d)
            } catch (g) {}
            return a.toString()
        },
        Pp = function(a, b) {
            b.id = "ima_html5";
            var c = Qp();
            b.c = a.g;
            b.domain = c.g;
            return b
        },
        Qp = function() {
            var a = F(),
                b = document;
            return new Fn(a.parent == a ? a.location.href : b.referrer)
        };
    var Tp = function() {
        G.call(this);
        this.w = this.D = this.I = this.F = !1;
        this.h = 0;
        this.o = [];
        this.B = !1;
        this.L = this.K = Infinity;
        this.l = 0;
        this.v = new Ep(this);
        this.G = {}
    };
    y(Tp, G);
    var Vp = function(a, b) {
            null == b || a.F || (a.g = b, Up(a), a.F = !0)
        },
        Xp = function(a) {
            null != a.g && a.F && (Wp(a), a.F = !1, a.D = !1, a.w = !1, a.h = 0, a.o = [], a.B = !1)
        },
        Up = function(a) {
            Wp(a);
            !(a.g instanceof G) && "ontouchstart" in document.documentElement && rp() ? (a.G = {
                touchstart: w(a.X, a),
                touchmove: w(a.U, a),
                touchend: w(a.V, a)
            }, Pa(a.G, function(a, c) {
                this.g.addEventListener(c, a, !1)
            }, a)) : a.v.N(a.g, "click", a.P)
        },
        Wp = function(a) {
            a.v.Na(a.g, "click", a.P);
            Pa(a.G, function(a, c) {
                this.g.removeEventListener(c, a, !1)
            }, a);
            a.G = {}
        };
    Tp.prototype.X = function(a) {
        this.D = !0;
        this.h = a.touches.length;
        this.l && (window.clearTimeout(this.l), this.l = 0, this.I = !0);
        (this.B = Yp(this, a.touches) || 1 != a.touches.length) ? this.L = this.K = Infinity: (this.K = a.touches[0].clientX, this.L = a.touches[0].clientY);
        a = a.touches;
        this.o = [];
        for (var b = 0; b < a.length; b++) this.o.push(a[b].identifier)
    };
    Tp.prototype.U = function(a) {
        this.h = a.touches.length;
        if (!tp(8) || Math.pow(a.changedTouches[0].clientX - this.K, 2) + Math.pow(a.changedTouches[0].clientY - this.L, 2) > Math.pow(5, 2)) this.w = !0
    };
    Tp.prototype.V = function(a) {
        !this.D || 1 != this.h || this.w || this.I || this.B || !Yp(this, a.changedTouches) || (this.l = window.setTimeout(w(this.O, this), 300));
        this.h = a.touches.length;
        0 == this.h && (this.w = this.D = !1, this.o = []);
        this.I = !1
    };
    Tp.prototype.P = function() {
        this.O()
    };
    var Yp = function(a, b) {
        for (var c = 0; c < b.length; c++)
            if (a.o.includes(b[c].identifier)) return !0;
        return !1
    };
    Tp.prototype.O = function() {
        this.l = 0;
        this.dispatchEvent(new zd("click"))
    };
    Tp.prototype.T = function() {
        Xp(this);
        this.v.W();
        this.v = null;
        Tp.da.T.call(this)
    };
    var Zp = function(a) {
        G.call(this);
        this.g = a || "goog_" + Tb++;
        this.l = []
    };
    y(Zp, G);
    Zp.prototype.h = !1;
    Zp.prototype.connect = function() {
        for (this.h = !0; 0 != this.l.length;) {
            var a = this.l.shift();
            this.sendMessage(a.name, a.type, a.data)
        }
    };
    var $p = function(a, b, c, d) {
            a.h ? a.sendMessage(b, c, d) : a.l.push({
                name: b,
                type: c,
                data: d
            })
        },
        aq = function(a, b, c, d, e) {
            zd.call(this, a);
            this.ha = b;
            this.ga = c;
            this.Jb = d;
            this.hd = e
        };
    y(aq, zd);
    aq.prototype.toString = function() {
        return ""
    };
    var bq = function(a, b) {
        Zp.call(this, b);
        this.o = a;
        this.va = null;
        this.v = new Ep(this);
        this.v.N(F(), "message", this.w)
    };
    y(bq, Zp);
    var cq = function(a) {
        if (null == a || !q(a) || 0 != a.lastIndexOf("ima://", 0)) return null;
        a = a.substr(6);
        try {
            return JSON.parse(a)
        } catch (b) {
            return null
        }
    };
    bq.prototype.sendMessage = function(a, b, c) {
        null != this.va && null != this.va.postMessage && this.va.postMessage(dq(this, a, b, c), "*");
        null != this.va && null == this.va.postMessage && Sp(Op.C(), 11)
    };
    bq.prototype.T = function() {
        this.v.W();
        bq.da.T.call(this)
    };
    bq.prototype.w = function(a) {
        a = a.h;
        var b = cq(a.data);
        if (eq(this, b)) {
            if (null == this.va) this.va = a.source, this.h || this.connect();
            else if (this.va != a.source) return;
            eq(this, b) && this.dispatchEvent(new aq(b.name, b.type, b.data || {}, b.sid, a.origin))
        }
    };
    var dq = function(a, b, c, d) {
            var e = {};
            e.name = b;
            e.type = c;
            null != d && (e.data = d);
            e.sid = a.g;
            e.channel = a.o;
            return "ima://" + Ae(e)
        },
        eq = function(a, b) {
            if (null == b) return !1;
            var c = b.channel;
            if (null == c || c != a.o) return !1;
            b = b.sid;
            return null == b || "*" != a.g && b != a.g ? !1 : !0
        };
    var fq = function(a, b) {
        G.call(this);
        this.o = a;
        this.l = b;
        this.g = {};
        this.h = new Ep(this);
        this.h.N(F(), "message", this.v)
    };
    y(fq, G);
    var gq = function(a, b) {
            var c = b.h;
            a.g.hasOwnProperty(c) && $p(a.g[c], b.type, b.ha, b.ga)
        },
        iq = function(a, b, c, d) {
            a.g.hasOwnProperty(b) || (c = new bq(b, c), a.h.N(c, a.o, function(a) {
                this.dispatchEvent(new hq(a.type, a.ha, a.ga, a.Jb, a.hd, b))
            }), c.va = d, c.connect(), a.g[b] = c)
        };
    fq.prototype.T = function() {
        this.h.W();
        for (var a in this.g) yd(this.g[a]);
        fq.da.T.call(this)
    };
    fq.prototype.v = function(a) {
        a = a.h;
        var b = cq(a.data);
        if (null != b) {
            var c = b.channel;
            if (this.l && !this.g.hasOwnProperty(c)) {
                var d = b.sid;
                iq(this, c, d, a.source);
                this.dispatchEvent(new hq(b.name, b.type, b.data || {}, d, a.origin, c))
            }
        }
    };
    var hq = function(a, b, c, d, e, f) {
        aq.call(this, a, b, c, d, e);
        this.h = f
    };
    y(hq, aq);
    var kq = function() {
        var a = Ca("google.ima.gptProxyInstance", F());
        if (null != a) return a;
        Ep.call(this);
        this.l = new fq("gpt", !0);
        xd(this, Oa(yd, this.l));
        this.N(this.l, "gpt", this.A);
        this.g = null;
        jq() || F().top === F() || (this.g = new fq("gpt", !1), xd(this, Oa(yd, this.g)), this.N(this.g, "gpt", this.v))
    };
    y(kq, Ep);
    var jq = function() {
            return !!Ca("googletag.cmd", F())
        },
        lq = function() {
            var a = Ca("googletag.console", F());
            return null != a ? a : null
        };
    kq.prototype.A = function(a) {
        var b = a.hd,
            c = "//imasdk.googleapis.com".match(He);
        b = b.match(He);
        if (c[3] == b[3] && c[4] == b[4])
            if (null != this.g) iq(this.g, a.h, a.Jb, F().parent), null != this.g && gq(this.g, a);
            else if (c = a.ga, null != c && p(c.scope)) {
            b = c.scope;
            c = c.args;
            var d;
            if ("proxy" == b) c = a.ha, "isGptPresent" == c ? d = jq() : "isConsolePresent" == c && (d = null != lq());
            else if (jq())
                if ("pubads" == b || "companionAds" == b) {
                    d = a.ha;
                    var e = F().googletag;
                    if (null != e && null != e[b] && (e = e[b](), null != e && (d = e[d], null != d))) try {
                        var f = d.apply(e, c)
                    } catch (g) {}
                    d =
                        f
                } else if ("console" == b) {
                if (f = lq(), null != f && (e = f[a.ha], null != e)) try {
                    e.apply(f, c)
                } catch (g) {}
            } else if (null === b) {
                d = a.ha;
                f = F();
                if (["googleGetCompanionAdSlots", "googleSetCompanionAdContents"].includes(d) && (d = f[d], null != d)) try {
                    e = d.apply(f, c)
                } catch (g) {}
                d = e
            }
            p(d) && (a.ga.returnValue = d, gq(this.l, a))
        }
    };
    kq.prototype.v = function(a) {
        gq(this.l, a)
    };
    var mq = function() {
        G.call(this)
    };
    y(mq, G);
    var nq = {
        tg: "autoplayDisallowed",
        vg: "beginFullscreen",
        CLICK: "click",
        Jg: "end",
        Kg: "endFullscreen",
        Lg: "error",
        Rg: "focusSkipButton",
        LOADED: "loaded",
        nh: "mediaLoadTimeout",
        Ec: "pause",
        Lh: "play",
        $h: "skip",
        ai: "skipShown",
        Fc: "start",
        ii: "timeUpdate",
        gi: "timedMetadata",
        ti: "volumeChange"
    };
    mq.prototype.Xc = function() {
        return !0
    };
    mq.prototype.reset = function(a) {
        this.pc() || this.zb() || this.pause();
        rp() && !a && (this.lb(.001), this.load("", ""));
        qp() && this.ib() && !a && this.Wb()
    };
    var oq = function() {
        this.v = this.F = this.o = this.l = this.h = null;
        this.J = this.D = this.H = this.B = this.w = !1;
        this.timeout = -1;
        this.g = !1;
        this.A = null
    };
    var qq = function(a, b) {
            var c = Array.prototype.slice.call(arguments),
                d = c.shift();
            if ("undefined" == typeof d) throw Error("[goog.string.format] Template required");
            return d.replace(/%([0\- \+]*)(\d+)?(\.(\d+))?([%sfdiu])/g, function(a, b, d, k, m, l, t, H) {
                if ("%" == l) return "%";
                var e = c.shift();
                if ("undefined" == typeof e) throw Error("[goog.string.format] Not enough arguments");
                arguments[0] = e;
                return pq[l].apply(null, arguments)
            })
        },
        pq = {
            s: function(a, b, c) {
                return isNaN(c) || "" == c || a.length >= Number(c) ? a : a = -1 < b.indexOf("-", 0) ?
                    a + Ob(" ", Number(c) - a.length) : Ob(" ", Number(c) - a.length) + a
            },
            f: function(a, b, c, d, e) {
                d = a.toString();
                isNaN(e) || "" == e || (d = parseFloat(a).toFixed(e));
                var f = 0 > Number(a) ? "-" : 0 <= b.indexOf("+") ? "+" : 0 <= b.indexOf(" ") ? " " : "";
                0 <= Number(a) && (d = f + d);
                if (isNaN(c) || d.length >= Number(c)) return d;
                d = isNaN(e) ? Math.abs(Number(a)).toString() : Math.abs(Number(a)).toFixed(e);
                a = Number(c) - d.length - f.length;
                return d = 0 <= b.indexOf("-", 0) ? f + d + Ob(" ", a) : f + Ob(0 <= b.indexOf("0", 0) ? "0" : " ", a) + d
            },
            d: function(a, b, c, d, e, f, g, k) {
                return pq.f(parseInt(a,
                    10), b, c, d, 0, f, g, k)
            }
        };
    pq.i = pq.d;
    pq.u = pq.d;
    var tq = function(a, b) {
        G.call(this);
        this.l = new Ep(this);
        this.F = !1;
        this.G = "goog_" + Tb++;
        this.D = new Cn;
        var c = this.G,
            d = T.Y() ? (Jf() ? "https:" : "http:") + qq("//imasdk.googleapis.com/js/core/admob/bridge_%s.html", T.w) : (Jf() ? "https:" : "http:") + qq("//imasdk.googleapis.com/js/core/bridge3.217.0_%s.html", T.w);
        a: {
            var e = window;
            try {
                do {
                    try {
                        if (0 == e.location.href.indexOf(d) || 0 == e.document.referrer.indexOf(d)) {
                            var f = !0;
                            break a
                        }
                    } catch (g) {}
                    e = e.parent
                } while (e != e.top)
            } catch (g) {}
            f = !1
        }
        f && (d += "?f=" + c);
        c = ld("IFRAME", {
            src: d + "#" +
                c,
            allowFullscreen: !0,
            allow: "autoplay",
            style: "border:0; opacity:0; margin:0; padding:0; position:relative;"
        });
        Hp(this.l, c, "load", this.Rd, void 0);
        a.appendChild(c);
        this.h = c;
        this.w = rq(this);
        this.B = b;
        this.g = this.B.h;
        this.v = this.o = null;
        this.l.N(this.w, "mouse", this.I);
        this.l.N(this.w, "touch", this.L);
        null != this.g && (this.l.N(this.w, "displayContainer", this.Xd), this.l.N(this.w, "videoDisplay", this.K), this.l.N(this.w, "preloadVideoDisplay", this.Yd), sq(this, this.g, this.Bb));
        a = F();
        b = Ca("google.ima.gptProxyInstance",
            a);
        null == b && (b = new kq, u("google.ima.gptProxyInstance", b, a))
    };
    y(tq, G);
    var rq = function(a, b) {
            b = b || "*";
            var c = a.D.get(b);
            null == c && (c = new bq(a.G, b), a.F && (c.va = qd(a.h), c.connect()), a.D.set(b, c));
            return c
        },
        vq = function(a, b) {
            null != a.g && uq(a, a.g, a.Bb);
            a.g = b;
            sq(a, a.g, a.Bb)
        };
    tq.prototype.T = function() {
        this.l.W();
        null !== this.v && (this.v.W(), this.v = null);
        sj(this.D.qb(!1), function(a) {
            a.W()
        });
        this.D.clear();
        nd(this.h);
        tq.da.T.call(this)
    };
    tq.prototype.I = function(a) {
        var b = a.ga,
            c = df(this.h),
            d = document.createEvent("MouseEvent");
        d.initMouseEvent(a.ha, !0, !0, window, b.detail, b.screenX, b.screenY, b.clientX + c.x, b.clientY + c.y, b.ctrlKey, b.altKey, b.shiftKey, b.metaKey, b.button, null);
        if (!ue || rp() || 0 == document.webkitIsFullScreen) this.h.blur(), window.focus();
        this.h.dispatchEvent(d)
    };
    var wq = function(a, b) {
        var c = df(a.h),
            d = !!("TouchEvent" in window && 0 < TouchEvent.length);
        b = b.map(function(b) {
            return d ? new Touch({
                identifier: b.identifier,
                target: a.h,
                clientX: b.clientX,
                clientY: b.clientY,
                screenX: b.screenX,
                screenY: b.screenY,
                pageX: b.pageX + c.x,
                pageY: b.pageY + c.y
            }) : document.createTouch(window, a.h, b.identifier, b.pageX + c.x, b.pageY + c.y, b.screenX, b.screenY)
        });
        return d ? b : document.createTouchList.apply(document, b)
    };
    tq.prototype.L = function(a) {
        var b = a.ga,
            c = df(this.h);
        if ("TouchEvent" in window && 0 < TouchEvent.length) b = {
            bubbles: !0,
            cancelable: !0,
            view: window,
            detail: b.detail,
            ctrlKey: b.ctrlKey,
            altKey: b.altKey,
            shiftKey: b.shiftKey,
            metaKey: b.metaKey,
            touches: wq(this, b.touches),
            targetTouches: wq(this, b.targetTouches),
            changedTouches: wq(this, b.changedTouches)
        }, a = new TouchEvent(a.ha, b), this.h.dispatchEvent(a);
        else {
            var d = document.createEvent("TouchEvent");
            d.initTouchEvent(a.ha, !0, !0, window, b.detail, b.screenX, b.screenY, b.clientX +
                c.x, b.clientY + c.y, b.ctrlKey, b.altKey, b.shiftKey, b.metaKey, wq(this, b.touches), wq(this, b.targetTouches), wq(this, b.changedTouches), b.scale, b.rotation);
            this.h.dispatchEvent(d)
        }
    };
    tq.prototype.K = function(a) {
        if (null != this.g) {
            var b = a.ga;
            switch (a.ha) {
                case "startTracking":
                    this.g.ec();
                    break;
                case "stopTracking":
                    this.g.gb();
                    break;
                case "exitFullscreen":
                    this.g.Wb();
                    break;
                case "play":
                    this.g.yb();
                    break;
                case "pause":
                    this.g.pause();
                    break;
                case "load":
                    this.g.load(b.videoUrl, b.mimeType);
                    break;
                case "setCurrentTime":
                    this.g.lb(b.currentTime);
                    break;
                case "setPlaybackOptions":
                    this.g.Ac(xq(b));
                    break;
                case "setVolume":
                    this.g.Kb(b.volume)
            }
        }
    };
    var xq = function(a) {
        a = a.playbackOptions;
        var b = new oq;
        b.h = a.adFormat;
        b.o = a.adSenseAgcid;
        b.F = a.contentVideoDocId;
        b.v = a.ctaAnnotationTrackingEvents;
        a.showAnnotations && (b.H = !0);
        a.viewCountsDisabled && (b.D = !0);
        b.timeout = a.loadVideoTimeout;
        a.ibaDisabled && (b.w = !0);
        a.enablePreloading && (b.g = !0);
        b.l = a.adQemId;
        a.isPharma && (b.B = !0);
        a.useAutoplayFlag && (b.J = !0);
        b.A = a.endscreenAdTracking;
        return b
    };
    h = tq.prototype;
    h.Yd = function(a) {
        if (null != this.o) {
            var b = a.ga;
            switch (a.ha) {
                case "startTracking":
                    this.o.ec();
                    break;
                case "stopTracking":
                    this.o.gb();
                    break;
                case "setPlaybackOptions":
                    this.o.Ac(xq(b));
                    break;
                case "load":
                    this.o.load(b.videoUrl, b.mimeType)
            }
        }
    };
    h.wc = function(a) {
        switch (a.type) {
            case "error":
                a = "error";
                break;
            case "loaded":
                a = "loaded";
                break;
            default:
                return
        }
        $p(this.w, "preloadVideoDisplay", a, {})
    };
    h.Bb = function(a) {
        var b = {};
        switch (a.type) {
            case "autoplayDisallowed":
                a = "autoplayDisallowed";
                break;
            case "beginFullscreen":
                a = "fullscreen";
                break;
            case "endFullscreen":
                a = "exitFullscreen";
                break;
            case "click":
                a = "click";
                break;
            case "end":
                a = "end";
                break;
            case "error":
                a = "error";
                break;
            case "loaded":
                a = "loaded";
                break;
            case "mediaLoadTimeout":
                a = "mediaLoadTimeout";
                break;
            case "pause":
                a = "pause";
                b.ended = this.g.zb();
                break;
            case "play":
                a = "play";
                break;
            case "skip":
                a = "skip";
                break;
            case "start":
                a = "start";
                break;
            case "timeUpdate":
                a =
                    "timeupdate";
                b.currentTime = this.g.xa();
                b.duration = this.g.wb();
                break;
            case "volumeChange":
                a = "volumeChange";
                b.volume = this.g.Tc();
                break;
            case "loadedmetadata":
                a = a.type;
                b.duration = this.g.wb();
                break;
            case "abort":
            case "canplay":
            case "canplaythrough":
            case "durationchange":
            case "emptied":
            case "loadstart":
            case "loadeddata":
            case "progress":
            case "ratechange":
            case "seeked":
            case "seeking":
            case "stalled":
            case "suspend":
            case "waiting":
                a = a.type;
                break;
            default:
                return
        }
        $p(this.w, "videoDisplay", a, b)
    };
    h.Xd = function(a) {
        switch (a.ha) {
            case "showVideo":
                null == this.v ? (this.v = new Tp, this.l.N(this.v, "click", this.Uf)) : Xp(this.v);
                Vp(this.v, yq(this.B));
                a = this.B;
                null != a.g && a.g.show();
                break;
            case "hide":
                null !== this.v && (this.v.W(), this.v = null);
                a = this.B;
                null != a.g && zq(a.g.g, !1);
                break;
            case "getPreloadDisplay":
                null != this.g && null == this.o && (this.o = this.B.l, sq(this, this.o, this.wc));
                break;
            case "swapVideoDisplays":
                if (null != this.g && null != this.o) {
                    uq(this, this.g, this.Bb);
                    uq(this, this.o, this.wc);
                    a = this.B;
                    if (a.g && a.h && a.o &&
                        a.l) {
                        var b = a.h;
                        a.h = a.l;
                        a.l = b;
                        b = a.g;
                        a.g = a.o;
                        a.o = b;
                        null != a.w && vq(a.w, a.h)
                    }
                    this.g = this.B.h;
                    this.o = this.B.l;
                    sq(this, this.g, this.Bb);
                    sq(this, this.o, this.wc)
                }
        }
    };
    h.Uf = function() {
        $p(this.w, "displayContainer", "videoClick")
    };
    h.Rd = function() {
        sj(this.D.qb(!1), function(a) {
            a.va = qd(this.h);
            a.connect()
        }, this);
        this.F = !0
    };
    var sq = function(a, b, c) {
            a.l.N(b, Ua(nq), c);
            a.l.N(b, jp, c)
        },
        uq = function(a, b, c) {
            a.l.Na(b, Ua(nq), c);
            a.l.Na(b, jp, c)
        };
    var Aq = function(a) {
        if (A(Qb(a))) return null;
        var b = a.match(/^https?:\/\/[^\/]*youtu\.be\/([a-zA-Z0-9_-]+)$/);
        if (null != b && 2 == b.length) return b[1];
        b = a.match(/^https?:\/\/[^\/]*youtube.com\/video\/([a-zA-Z0-9_-]+)$/);
        if (null != b && 2 == b.length) return b[1];
        b = a.match(/^https?:\/\/[^\/]*youtube.com\/watch\/([a-zA-Z0-9_-]+)$/);
        if (null != b && 2 == b.length) return b[1];
        a = (new Fn(a)).h;
        return Wn(a, "v") ? a.get("v").toString() : Wn(a, "video_id") ? a.get("video_id").toString() : null
    };
    var Bq = function() {};
    Bq.prototype.allowCustom = !0;
    var Cq = {
            eh: "Image",
            Qg: "Flash",
            yd: "All"
        },
        Dq = {
            Yg: "Html",
            ah: "IFrame",
            ci: "Static",
            yd: "All"
        },
        Eq = {
            bh: "IgnoreSize",
            Xh: "SelectExactMatch",
            Yh: "SelectNearMatch"
        },
        Fq = {
            Gg: "DisallowResize",
            Sh: "ResizeSmaller"
        };
    var Gq = !1,
        Hq = function(a) {
            if (a = a.match(/[\d]+/g)) a.length = 3
        };
    (function() {
        if (navigator.plugins && navigator.plugins.length) {
            var a = navigator.plugins["Shockwave Flash"];
            if (a && (Gq = !0, a.description)) {
                Hq(a.description);
                return
            }
            if (navigator.plugins["Shockwave Flash 2.0"]) {
                Gq = !0;
                return
            }
        }
        if (navigator.mimeTypes && navigator.mimeTypes.length && (a = navigator.mimeTypes["application/x-shockwave-flash"], Gq = !(!a || !a.enabledPlugin))) {
            Hq(a.enabledPlugin.description);
            return
        }
        try {
            var b = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
            Gq = !0;
            Hq(b.GetVariable("$version"));
            return
        } catch (c) {}
        try {
            b =
                new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
            Gq = !0;
            return
        } catch (c) {}
        try {
            b = new ActiveXObject("ShockwaveFlash.ShockwaveFlash"), Gq = !0, Hq(b.GetVariable("$version"))
        } catch (c) {}
    })();
    var Iq = Gq;
    var Kq = function(a, b) {
            b = void 0 === b ? null : b;
            if (null == a || 0 >= a.width || 0 >= a.height) throw Dp(Cp, null, "ad slot size", a.toString());
            this.h = a;
            this.g = null != b ? b : new Bq;
            this.v = Jq(Dq, this.g.resourceType) ? this.g.resourceType : "All";
            this.o = Jq(Cq, this.g.creativeType) ? this.g.creativeType : "All";
            this.w = Jq(Eq, this.g.sizeCriteria) ? this.g.sizeCriteria : "SelectExactMatch";
            this.B = Jq(Fq, this.g.g) ? this.g.g : "DisallowResize";
            this.l = null != this.g.adSlotIds ? this.g.adSlotIds : [];
            this.A = r(this.g.nearMatchPercent) && 0 < this.g.nearMatchPercent &&
                100 >= this.g.nearMatchPercent ? this.g.nearMatchPercent : 90
        },
        Nq = function(a, b) {
            var c = [];
            b.forEach(function(b) {
                a.g.allowCustom && (!A(b.h) && (isNaN(b.A) || isNaN(b.v) || b.v == b.A) && Lq(a, b) ? c.push(b) : (b = Mq(a, b), null != b && !A(b.h) && c.push(b)))
            });
            return c
        },
        Lq = function(a, b) {
            var c;
            if (c = "Flash" != b.g || Iq) {
                if (c = "All" == a.v || a.v == b.J) c = b.g, c = null == c ? !0 : "All" == a.o || a.o == c;
                c && (c = b.D, c = 0 == a.l.length ? !0 : null != c ? a.l.includes(c) : !1)
            }
            if (c)
                if (b = b.l, "IgnoreSize" == a.w || $c(a.h, b)) a = !0;
                else {
                    if (c = "SelectNearMatch" == a.w) "ResizeSmaller" ==
                        a.B ? (b.width <= a.h.width && b.height <= a.h.height || (c = a.h, c = Math.min(c.width / b.width, c.height / b.height), b = new E(c * b.width, c * b.height)), c = b.width, b = b.height) : (c = b.width, b = b.height), c = c > a.h.width || b > a.h.height || c < a.A / 100 * a.h.width || b < a.A / 100 * a.h.height ? !1 : !0;
                    a = c
                }
            else a = !1;
            return a
        },
        Mq = function(a, b) {
            b = b.o;
            return null == b ? null : b.find(function(b) {
                return Lq(a, b)
            }) || null
        },
        Jq = function(a, b) {
            return null != b && Ya(a, b)
        };
    var Oq = function(a) {
        var b = {};
        a.split(",").forEach(function(a) {
            var c = a.split("=");
            2 == c.length && (a = Db(c[0]), c = Db(c[1]), 0 < a.length && (b[a] = c))
        });
        return b
    };
    var Pq = function(a) {
        this.h = a.content;
        this.g = a.contentType;
        this.l = a.size;
        this.v = a.masterSequenceNumber;
        this.J = a.resourceType;
        this.A = a.sequenceNumber;
        this.D = a.adSlotId;
        this.o = [];
        a = a.backupCompanions;
        null != a && (this.o = a.map(function(a) {
            return new Pq(a)
        }))
    };
    Pq.prototype.getContent = function() {
        return this.h
    };
    Pq.prototype.w = function() {
        return this.g
    };
    Pq.prototype.H = function() {
        return this.l.width
    };
    Pq.prototype.B = function() {
        return this.l.height
    };
    var Qq = function() {
        this.A = 1;
        this.l = -1;
        this.g = 1;
        this.v = this.o = 0;
        this.h = !1
    };
    h = Qq.prototype;
    h.se = function() {
        return this.A
    };
    h.pe = function() {
        return this.l
    };
    h.ne = function() {
        return this.g
    };
    h.qe = function() {
        return this.o
    };
    h.re = function() {
        return this.v
    };
    h.oe = function() {
        return this.h
    };
    var X = function(a) {
        this.g = a
    };
    X.prototype.h = function() {
        return this.g.adId
    };
    X.prototype.l = function() {
        return this.g.creativeAdId
    };
    X.prototype.o = function() {
        return this.g.creativeId
    };
    var Rq = function(a) {
        return a.g.adQueryId
    };
    h = X.prototype;
    h.ue = function() {
        return this.g.adSystem
    };
    h.ve = function() {
        return this.g.cdertiserName
    };
    h.we = function() {
        return this.g.apiFramework
    };
    h.Ne = function() {
        return this.g.adWrapperIds
    };
    h.Pe = function() {
        return this.g.adWrapperCreativeIds
    };
    h.Oe = function() {
        return this.g.adWrapperSystems
    };
    h.Qe = function() {
        return this.g.linear
    };
    h.Re = function() {
        return this.g.skippable
    };
    h.ye = function() {
        return this.g.contentType
    };
    h.Sd = function() {
        return this.g.description
    };
    h.Ud = function() {
        return this.g.title
    };
    h.cc = function() {
        return this.g.duration
    };
    h.Le = function() {
        return this.g.vastMediaWidth
    };
    h.Ke = function() {
        return this.g.vastMediaHeight
    };
    h.Me = function() {
        return this.g.width
    };
    h.Ae = function() {
        return this.g.height
    };
    h.He = function() {
        return this.g.uiElements
    };
    h.Ce = function() {
        return this.g.minSuggestedDuration
    };
    h.te = function() {
        var a = this.g.adPodInfo,
            b = new Qq;
        b.o = a.podIndex;
        b.v = a.timeOffset;
        b.A = a.totalAds;
        b.g = a.adPosition;
        b.h = a.isBumper;
        b.l = a.maxDuration;
        return b
    };
    h.xe = function(a, b, c) {
        var d = this.g.companions.map(function(a) {
            return new Pq(a)
        });
        return Nq(new Kq(new E(a, b), c), d)
    };
    h.Fe = function() {
        return Oq(Qb(this.g.traffickingParameters))
    };
    h.Ge = function() {
        return this.g.traffickingParameters
    };
    h.Be = function() {
        return this.g.mediaUrl
    };
    h.Ee = function() {
        return this.g.surveyUrl
    };
    h.ze = function() {
        return this.g.dealId
    };
    h.Je = function() {
        return this.g.universalAdIdValue
    };
    h.Ie = function() {
        return this.g.universalAdIdRegistry
    };
    h.De = function() {
        return this.g.skipTimeOffset
    };
    h.Se = function() {
        return this.g.disableUi
    };
    var Sq = function() {
            G.call(this);
            this.g = null;
            this.F = new Ep(this);
            xd(this, Oa(yd, this.F));
            this.h = new Map;
            this.o = new Map;
            this.w = this.v = !1;
            this.B = new Ng;
            this.l = !1;
            this.D = null
        },
        Tq;
    y(Sq, G);
    var Uq = null,
        Vq = function() {
            null == Uq && (Uq = new Sq);
            return Uq
        };
    Sq.prototype.Bc = function(a, b) {
        var c = {};
        c.queryId = a;
        c.viewabilityString = b;
        this.g ? $p(this.g, "activityMonitor", "measurableImpression", c) : this.dispatchEvent(new S("measurable_impression", null, c))
    };
    var Lm = function(a, b, c) {
            var d = {};
            d.queryId = b;
            d.viewabilityData = c;
            a.g && $p(a.g, "activityMonitor", "viewabilityMeasurement", d)
        },
        xm = function(a, b, c, d) {
            var e = {};
            e.queryId = b;
            e.viewabilityString = c;
            e.eventName = d;
            a.g ? $p(a.g, "activityMonitor", "externalActivityEvent", e) : a.dispatchEvent(new S("externalActivityEvent", null, e))
        };
    Sq.prototype.T = function() {
        this.F.Na(this.g, "activityMonitor", this.G);
        this.l = !1;
        this.h.clear();
        this === Tq && (Tq = null);
        Sq.da.T.call(this)
    };
    var Xq = function(a) {
            if (null == a) return !1;
            if (qp() && null != a.webkitDisplayingFullscreen) return a.webkitDisplayingFullscreen;
            var b = window.screen.availWidth || window.screen.width,
                c = window.screen.availHeight || window.screen.height;
            a = Wq(a);
            return 0 >= b - a.width && 42 >= c - a.height
        },
        Wq = function(a) {
            var b = {
                left: a.offsetLeft,
                top: a.offsetTop,
                width: a.offsetWidth,
                height: a.offsetHeight
            };
            try {
                v(a.getBoundingClientRect) && pd(bd(a), a) && (b = a.getBoundingClientRect())
            } catch (c) {}
            return b
        },
        Yq = function(a, b, c, d, e) {
            if (a.l) {
                e = e || {};
                d &&
                    null == e.opt_osdId && (e.opt_osdId = d);
                if (a.D) return a.D(b, c, e);
                if (a = d ? a.o.get(d) : T.D) null == e.opt_fullscreen && (e.opt_fullscreen = Xq(a)), null == e.opt_adElement && (e.opt_adElement = a);
                return Ah("lidar::handlevast_html5", Oa(An, b, c, e)) || {}
            }
            return {}
        };
    Sq.prototype.K = function(a) {
        this.w = a
    };
    Sq.prototype.I = function() {
        return this.w
    };
    Sq.prototype.L = function(a) {
        this.B = new Ng(a.adk, a.awbidKey)
    };
    var Zq = function(a, b) {
            var c = dp(),
                d = String(Math.floor(1E9 * Math.random()));
            a.o.set(d, b);
            if (gp(c, 31061775)) try {
                nf(function(b) {
                    if (a.g) {
                        var c = {};
                        c.engagementString = b;
                        $p(a.g, "activityMonitor", "engagementData", c)
                    }
                }, function() {
                    return b
                })
            } catch (e) {}
            0 != T.g && yn(R.C(), d, a);
            return d
        },
        $q = function(a, b, c) {
            if (c) a.h.get(c) == b && a.h["delete"](c);
            else {
                var d = [];
                a.h.forEach(function(a, c) {
                    a == b && d.push(c)
                });
                d.forEach(a.h["delete"], a.h)
            }
        },
        Hm = function(a, b) {
            a = a.h.get(b);
            return v(a) ? a() : {}
        },
        ar = function(a) {
            if (v(window.Goog_AdSense_Lidar_getUrlSignalsArray)) {
                var b = {};
                b.pageSignals = window.Goog_AdSense_Lidar_getUrlSignalsArray();
                $p(a.g, "activityMonitor", "pageSignals", b)
            }
        };
    Sq.prototype.G = function(a) {
        var b = a.ga,
            c = b.queryId,
            d = {},
            e = null;
        d.eventId = b.eventId;
        switch (a.ha) {
            case "getPageSignals":
                ar(this);
                break;
            case "reportVastEvent":
                e = b.vastEvent;
                a = b.osdId;
                var f = {};
                f.opt_fullscreen = b.isFullscreen;
                b.isOverlay && (f.opt_bounds = b.overlayBounds);
                d.viewabilityData = Yq(this, e, c, a, f);
                $p(this.g, "activityMonitor", "viewability", d);
                break;
            case "fetchAdTagUrl":
                c = {}, c.eventId = b.eventId, a = b.osdId, Xa(b, "isFullscreen") && (e = b.isFullscreen), Xa(b, "loggingId") && (b = b.loggingId, c.loggingId = b, Sp(Op.C(),
                    43, {
                        step: "beforeLookup",
                        logid: b,
                        time: x()
                    }, !0)), c.engagementString = br(this, a, e), this.g && $p(this.g, "activityMonitor", "engagement", c)
        }
    };
    var br = function(a, b, c) {
        var d = b ? a.o.get(b) : T.D;
        a = {};
        null != c && (a.fullscreen = c);
        c = "";
        try {
            c = mf(function() {
                return d
            }, a)
        } catch (e) {
            c = "sdktle;" + Nb(e.name, 12) + ";" + Nb(e.message, 40)
        }
        return c
    };
    u("ima.common.getVideoMetadata", function(a) {
        return Hm(Vq(), a)
    }, void 0);
    u("ima.common.triggerViewEvent", function(a, b) {
        var c = Vq(),
            d = {};
        d.queryId = a;
        d.viewabilityString = b;
        c.g ? $p(c.g, "activityMonitor", "viewableImpression", d) : c.dispatchEvent(new S("viewable_impression", null, d))
    }, void 0);
    u("ima.common.triggerViewabilityMeasurementUpdate", function(a, b) {
        Lm(Vq(), a, b)
    }, void 0);
    u("ima.common.triggerMeasurableEvent", function(a, b) {
        Vq().Bc(a, b)
    }, void 0);
    u("ima.common.triggerExternalActivityEvent", function(a, b, c) {
        xm(Vq(), a, b, c)
    }, void 0);
    var cr = Vq();
    var dr = function() {
        this.h = 0;
        this.g = []
    };
    h = dr.prototype;
    h.add = function(a) {
        var b = this.g[this.h];
        this.g[this.h] = a;
        this.h = (this.h + 1) % 4;
        return b
    };
    h.get = function(a) {
        a = er(this, a);
        return this.g[a]
    };
    h.set = function(a, b) {
        a = er(this, a);
        this.g[a] = b
    };
    h.Ga = function() {
        return this.g.length
    };
    h.isEmpty = function() {
        return 0 == this.g.length
    };
    h.clear = function() {
        this.h = this.g.length = 0
    };
    h.oa = function() {
        var a = this.Ga(),
            b = this.Ga(),
            c = [];
        for (a = this.Ga() - a; a < b; a++) c.push(this.get(a));
        return c
    };
    h.Ra = function() {
        for (var a = [], b = this.Ga(), c = 0; c < b; c++) a[c] = c;
        return a
    };
    var er = function(a, b) {
        if (b >= a.g.length) throw Error("Out of bounds exception");
        return 4 > a.g.length ? b : (a.h + Number(b)) % 4
    };
    var fr = function(a) {
        G.call(this);
        this.g = a;
        this.ka = "";
        this.O = -1;
        this.ma = !1;
        this.ra = new dr;
        this.v = 0;
        this.fa = this.G = this.o = this.L = this.V = this.F = !1;
        this.I = this.l = null;
        this.ca = this.xb();
        this.U = this.ib();
        this.wa = T.Y() ? 15E3 : 8E3;
        this.w = null;
        this.ea = !1
    };
    y(fr, mq);
    fr.prototype.Nc = function() {
        var a = this;
        return Ua(Vo).filter(function(b) {
            return !A(a.g.canPlayType(b))
        })
    };
    fr.prototype.Ac = function(a) {
        this.wa = 0 < a.timeout ? a.timeout : T.Y() ? 15E3 : 8E3;
        a.g && (this.g.preload = "auto")
    };
    var hr = function(a, b) {
        var c = 0 < a.g.seekable.length;
        a.ma ? c ? (a.g.currentTime = a.O, gr(a), b()) : setTimeout(function() {
            return hr(a, b)
        }, 100) : (gr(a), b())
    };
    fr.prototype.Gb = function() {
        this.ka = this.g.currentSrc;
        this.ma = 0 < this.g.seekable.length;
        this.O = this.g.ended ? -1 : this.g.currentTime
    };
    fr.prototype.X = function(a) {
        a = void 0 === a ? null : a;
        if (0 <= this.O) {
            var b = this,
                c = null == a ? function() {} : a;
            this.g.addEventListener("loadedmetadata", function e() {
                hr(b, c);
                b.g.removeEventListener("loadedmetadata", e, !1)
            }, !1);
            this.L = !1;
            this.g.src = this.ka;
            this.g.load()
        } else null != a && a()
    };
    var gr = function(a) {
        a.O = -1;
        a.ka = "";
        a.ma = !1
    };
    h = fr.prototype;
    h.load = function(a, b) {
        ir(this);
        b && T.Y() && v(this.g.g) && this.g.g(b);
        this.L = !1;
        a && (this.g.src = a);
        this.g.load()
    };
    h.Kb = function(a) {
        this.g.volume = a;
        this.g.muted = 0 == a ? !0 : !1
    };
    h.Tc = function() {
        return this.g.volume
    };
    h.yb = function() {
        var a = this;
        T.Nb() && !this.o && (fc() || gc()) && "hidden" == n.document.visibilityState ? this.w || (this.w = w(this.pa, this), n.document.addEventListener("visibilitychange", this.w)) : this.pa();
        this.ea = !1;
        this.L || cc() ? (this.G = !1, this.l = this.g.play(), null != this.l && (this.I = null, this.l.then(function() {
            a.l = null;
            a.gd(a.I);
            a.I = null
        })["catch"](function(b) {
            jr(a);
            a.l = null;
            var c = "";
            null != b && null != b.name && (c = b.name);
            "AbortError" == c || "NotAllowedError" == c ? a.dispatchEvent("autoplayDisallowed") : a.vc()
        }))) : this.G = !0
    };
    h.pause = function() {
        null == this.l && (this.ea = !0, this.g.pause(), jr(this))
    };
    h.pc = function() {
        return this.g.paused ? rp() || te ? this.g.currentTime < this.g.duration : !0 : !1
    };
    h.Wb = function() {
        qp() && this.g.webkitDisplayingFullscreen && this.g.webkitExitFullscreen()
    };
    h.ib = function() {
        return Xq(this.g)
    };
    h.lb = function(a) {
        this.g.currentTime = a
    };
    h.xa = function() {
        return this.g.currentTime
    };
    h.wb = function() {
        return isNaN(this.g.duration) ? -1 : this.g.duration
    };
    h.zb = function() {
        return this.g.ended
    };
    h.xb = function() {
        return new E(this.g.offsetWidth, this.g.offsetHeight)
    };
    h.T = function() {
        this.gb();
        fr.da.T.call(this)
    };
    h.ec = function() {
        this.gb();
        this.h = new Ep(this);
        this.h.N(this.g, jp, this.ya);
        this.h.N(this.g, "canplay", this.Mf);
        this.h.N(this.g, "ended", this.Nf);
        this.h.N(this.g, "webkitbeginfullscreen", this.fc);
        this.h.N(this.g, "webkitendfullscreen", this.Uc);
        this.h.N(this.g, "loadedmetadata", this.Of);
        this.h.N(this.g, "pause", this.Rf);
        this.h.N(this.g, "playing", this.gd);
        this.h.N(this.g, "timeupdate", this.Sf);
        this.h.N(this.g, "volumechange", this.Wf);
        this.h.N(this.g, "error", this.vc);
        this.h.N(this.g, vp() || rp() && !tp(8) ? "loadeddata" :
            "canplay", this.Pf);
        this.D = new Tp;
        this.h.N(this.D, "click", this.Df);
        Vp(this.D, this.g);
        this.K = new ee(1E3);
        this.h.N(this.K, "tick", this.Ef);
        this.K.start()
    };
    h.gb = function() {
        null != this.D && (Xp(this.D), this.D = null);
        null != this.K && this.K.W();
        null != this.h && (this.h.W(), this.h = null);
        ir(this)
    };
    var ir = function(a) {
        a.V = !1;
        a.o = !1;
        a.F = !1;
        a.G = !1;
        a.v = 0;
        a.fa = !1;
        a.l = null;
        a.I = null;
        a.ra.clear();
        jr(a);
        yd(a.B)
    };
    fr.prototype.ya = function(a) {
        this.dispatchEvent(a.type)
    };
    var kr = function(a, b) {
        if (!a.o) {
            a.o = !0;
            jr(a);
            a.dispatchEvent("start");
            var c = v(a.g.getAttribute) && null != a.g.getAttribute("playsinline");
            (vc || tp(10) && T.o) && c || (!qp() || T.Y()) && (!tc || tc && sp(op, 4)) && !dl() || !tc || tc && sp(op, 3) || qp() && !tp(4) || a.fc(b)
        }
    };
    h = fr.prototype;
    h.Mf = function() {
        var a;
        if (a = ue) a = C, a = !(a && (B(a, "SMART-TV") || B(a, "SmartTV")));
        a && !this.fa && (this.lb(.001), this.fa = !0)
    };
    h.Of = function() {
        this.L = !0;
        this.G && this.yb();
        this.G = !1
    };
    h.Pf = function() {
        this.V || (this.V = !0, this.dispatchEvent("loaded"))
    };
    h.gd = function(a) {
        null != this.l ? this.I = a : (this.dispatchEvent("play"), rp() || vp() || kr(this, a))
    };
    h.Sf = function(a) {
        if (!this.o && (rp() || vp())) {
            if (0 >= this.xa()) return;
            if (vp() && this.zb() && 1 == this.wb()) {
                this.vc(a);
                return
            }
            kr(this, a)
        }
        if (rp() || up()) {
            if (1.5 < this.xa() - this.v) {
                this.F = !0;
                this.lb(this.v);
                return
            }
            this.F = !1;
            this.xa() > this.v && (this.v = this.xa())
        }
        this.ra.add(this.g.currentTime);
        this.dispatchEvent("timeUpdate")
    };
    h.Wf = function() {
        this.dispatchEvent("volumeChange")
    };
    h.Rf = function() {
        if (this.o && rp() && !this.ea && (2 > lr(this) || this.F)) {
            this.B = new ee(250);
            this.h.N(this.B, "tick", this.Lf);
            this.B.start();
            var a = !0
        } else a = !1;
        a || this.l || this.dispatchEvent("pause")
    };
    h.Nf = function() {
        var a = !0;
        if (rp() || up()) a = this.v >= this.g.duration - 1.5;
        !this.F && a && this.dispatchEvent("end")
    };
    h.fc = function() {
        this.dispatchEvent("beginFullscreen")
    };
    h.Uc = function() {
        this.dispatchEvent("endFullscreen")
    };
    h.vc = function() {
        jr(this);
        this.dispatchEvent("error")
    };
    h.Df = function() {
        this.dispatchEvent("click")
    };
    h.Ef = function() {
        var a = this.xb(),
            b = this.ib();
        if (a.width != this.ca.width || a.height != this.ca.height) !this.U && b ? this.fc() : this.U && !b && this.Uc(), this.ca = a, this.U = b
    };
    h.Nd = function() {
        if (!this.o) {
            try {
                Sp(Op.C(), 16)
            } catch (a) {}
            ir(this);
            this.dispatchEvent("mediaLoadTimeout")
        }
    };
    h.Lf = function() {
        if (this.zb() || !this.pc()) yd(this.B);
        else {
            var a = this.g.duration - this.g.currentTime,
                b = lr(this);
            0 < b && (2 <= b || 2 > a) && (yd(this.B), this.yb())
        }
    };
    var lr = function(a) {
        var b;
        a: {
            for (b = a.g.buffered.length - 1; 0 <= b;) {
                if (a.g.buffered.start(b) <= a.g.currentTime) {
                    b = a.g.buffered.end(b);
                    break a
                }
                b--
            }
            b = 0
        }
        return b - a.g.currentTime
    };
    fr.prototype.pa = function() {
        this.P || (this.P = fe(this.Nd, this.wa, this));
        mr(this)
    };
    var jr = function(a) {
            a.P && (n.clearTimeout(a.P), a.P = null);
            mr(a)
        },
        mr = function(a) {
            a.w && (n.document.removeEventListener("visibilitychange", a.w), a.w = null)
        };
    var nr = {},
        or = function(a, b) {
            var c = "key_" + a + ":" + b,
                d = nr[c];
            if (void 0 === d || 0 > d) nr[c] = 0;
            else if (0 == d) throw Error('Encountered two active delegates with the same priority ("' + a + ":" + b + '").');
        };
    or("a", "");
    or("a", "redesign2014q4");
    or("b", "");
    or("b", "redesign2014q4");
    or("b", "forcedlinebreak");
    var qr = function() {
        G.call(this);
        this.buffered = new pr;
        this.o = new pr;
        this.h = new Ep(this);
        this.l = "";
        this.v = !1;
        this.g = null;
        var a = Ko(T);
        if (a) {
            a: {
                if (Xa(a.g, "videoElementMockDuration") && (a = a.g.videoElementMockDuration, r(a))) break a;a = NaN
            }
            this.duration = a
        }
    };
    y(qr, G);
    var rr = new Cn,
        sr = function() {
            var a = ["video/mp4"],
                b = ["video/ogg"],
                c = new qr;
            c.canPlayType = function(c) {
                return a.includes(c) ? "probably" : b.includes(c) ? "maybe" : ""
            };
            c.width = 0;
            c.height = 0;
            c.offsetWidth = 0;
            c.offsetHeight = 0;
            return c
        },
        tr = function(a) {
            this.startTime = 0;
            this.endTime = a
        },
        pr = function() {
            this.length = 0;
            this.g = []
        };
    pr.prototype.start = function(a) {
        return this.g[a].startTime
    };
    pr.prototype.end = function(a) {
        return this.g[a].endTime
    };
    h = qr.prototype;
    h.readyState = 0;
    h.currentTime = 0;
    h.duration = NaN;
    h.gc = !0;
    h.autoplay = !1;
    h.loop = !1;
    h.controls = !1;
    h.volume = 1;
    h.muted = !1;
    Object.defineProperty(qr.prototype, "src", {
        get: function() {
            return qr.prototype.l
        },
        set: function(a) {
            var b = qr.prototype;
            b.v && null != b.g ? (b.g.reject(), b.g = null) : b.l = a
        }
    });
    h = qr.prototype;
    h.rb = null;
    h.Pb = null;
    h.pause = function() {
        this.autoplay = !1;
        this.gc || (null.stop(), this.gc = !0, this.dispatchEvent("timeupdate"), this.dispatchEvent("pause"))
    };
    h.load = function() {
        this.readyState = 0;
        this.gc = !0;
        this.dispatchEvent("loadstart");
        var a;
        isNaN(this.duration) ? a = 10 + 20 * Math.random() : a = this.duration;
        this.duration = Number(a);
        this.dispatchEvent("durationchange");
        a = this.o;
        a.g.push(new tr(this.duration));
        a.length = a.g.length;
        a = this.buffered;
        a.g.push(new tr(this.duration));
        a.length = a.g.length;
        this.dispatchEvent("loadedmetadata");
        0 < this.currentTime && this.dispatchEvent("timeupdate");
        this.dispatchEvent("loadeddata");
        this.dispatchEvent("canplay");
        this.dispatchEvent("canplaythrough");
        this.dispatchEvent("progress")
    };
    h.setAttribute = function(a, b) {
        null != a && rr.set(a, b)
    };
    h.T = function() {
        this.h.W()
    };
    h.Vf = function(a) {
        var b = null,
            c = null;
        switch (a.type) {
            case "loadeddata":
                b = "Loaded";
                break;
            case "playing":
                b = "Playing";
                c = "#00f";
                break;
            case "pause":
                b = "Paused";
                break;
            case "ended":
                b = "Ended", c = "#000"
        }
        b && this.Pb && (this.Pb.innerText = b);
        c && this.rb && (this.rb.style.backgroundColor = c)
    };
    var ur = function(a, b, c, d) {
        if (null == a || !pd(bd(a), a)) throw Dp(Cp, null, "containerElement", "element");
        this.v = a;
        this.h = this.g = null;
        this.o = b;
        this.w = !d;
        this.A = c;
        this.l = null;
        this.g = ld("DIV", {
            style: "display:none;"
        });
        this.v.appendChild(this.g);
        if (this.w) {
            a = Ko(T);
            if (Lo(a, "useVideoElementMock")) {
                a = sr();
                b = ld("DIV", {
                    style: "position:absolute;width:100%;height:100%;top:0px;left:0px;"
                });
                for (e in a) b[e] = a[e];
                a.rb = ld("DIV", {
                    style: "position:absolute;width:100%;height:100%;top:0px;left:0px;background-color:#000"
                });
                a.Pb =
                    ld("P", {
                        style: "position:absolute;top:25%;margin-left:10px;font-size:24px;color:#fff;"
                    });
                a.rb.appendChild(a.Pb);
                b.appendChild(a.rb);
                a.h.N(a, ["loadeddata", "playing", "pause", "ended"], a.Vf);
                var e = b
            } else e = ld("VIDEO", {
                style: "background-color:#000;position:absolute;width:100%;height:100%;left:0px;top:0px;",
                title: "Advertisement"
            });
            e.setAttribute("webkit-playsinline", !0);
            e.setAttribute("playsinline", !0);
            this.h = e;
            this.g.appendChild(this.h)
        }
        this.o && (e = ld("DIV", {
                id: this.o,
                style: "display:none;position:absolute;width:100%;height:100%;left:0px;top:0px;background-color:#000;"
            }),
            this.g.appendChild(e));
        this.A && (this.l = ld("DIV", {
            style: "position:absolute;width:100%;height:100%;left:0px;top:0px"
        }), this.g.appendChild(this.l))
    };
    y(ur, wd);
    ur.prototype.T = function() {
        nd(this.g);
        ur.da.T.call(this)
    };
    ur.prototype.show = function() {
        zq(this.g, !0)
    };
    var zq = function(a, b) {
        null != a && (a.style.display = b ? "block" : "none")
    };
    var yr = function(a) {
        G.call(this);
        this.K = "ima-chromeless-video";
        var b = null;
        null != a && (q(a) ? this.K = a : b = a);
        this.L = new Ep(this);
        this.v = null;
        this.o = !1;
        this.fa = this.xb();
        this.ea = this.ib();
        this.F = -1;
        this.U = !1;
        this.w = -1;
        this.g = this.P = this.G = null;
        this.ra = "";
        this.l = !1;
        this.ca = null != b;
        this.pa = this.I = this.V = this.h = null;
        this.B = void 0;
        this.ma = null;
        this.D = 0;
        this.ca ? (this.l = !0, this.h = b, this.B = 2) : (a = w(this.Od, this), vr ? a() : (wr.push(a), a = document.createElement("SCRIPT"), Yc(a, xr), b = document.getElementsByTagName("script")[0],
            b.parentNode.insertBefore(a, b)))
    };
    y(yr, mq);
    var xr = Pc(Kc(Lc("https://www.youtube.com/iframe_api"))),
        zr = {
            el: "adunit",
            controls: 0,
            html5: 1,
            playsinline: 1,
            ps: "gvn",
            showinfo: 0
        },
        wr = [],
        vr = !1;
    h = yr.prototype;
    h.Ac = function(a) {
        this.g = a
    };
    h.load = function(a, b) {
        null !== a && (this.ra = a, this.l ? Ar(this, a, b) : (this.G = a, this.P = b))
    };
    h.Kb = function(a) {
        this.ca ? this.dispatchEvent("volumeChange") : this.l ? (a = Math.min(Math.max(100 * a, 0), 100), this.h.setVolume(a), this.w = -1, this.dispatchEvent("volumeChange")) : this.w = a
    };
    h.Tc = function() {
        return this.l ? this.h.getVolume() / 100 : this.w
    };
    h.yb = function() {
        if (!A(Qb(this.ra))) {
            if (!this.o) {
                Br(this);
                var a = T.Y() ? 15E3 : 8E3;
                null != this.g && 0 < this.g.timeout && (a = this.g.timeout);
                this.ab = fe(this.Xb, a, this)
            }
            this.l ? (this.U = !1, !this.o && this.g && this.g.g ? this.h.loadVideoByPlayerVars(this.ma) : this.h.playVideo()) : this.U = !0
        }
    };
    h.pause = function() {
        this.l && this.o && this.h.pauseVideo()
    };
    h.pc = function() {
        return this.l ? 2 == this.h.getPlayerState(this.B) : !1
    };
    h.Wb = function() {};
    h.ib = function() {
        var a = document.getElementById(this.K);
        return a ? Xq(a) : !1
    };
    h.lb = function(a) {
        this.l ? this.h.seekTo(a, !1) : this.F = a
    };
    h.xa = function() {
        return this.l ? this.h.getCurrentTime(this.B) : -1
    };
    h.wb = function() {
        return this.l && this.o ? this.h.getDuration(this.B) : -1
    };
    h.Nc = function() {
        return Ua(Vo)
    };
    h.zb = function() {
        return this.l ? 0 == this.h.getPlayerState(this.B) : !1
    };
    h.xb = function() {
        var a = document.getElementById(this.K);
        return a ? new E(a.offsetWidth, a.offsetHeight) : new E(0, 0)
    };
    h.Xc = function() {
        return this.l ? 1 == this.h.getPlayerState(this.B) : !1
    };
    h.Ff = function() {
        var a = this.xb(),
            b = this.ib();
        if (a.width != this.fa.width || a.height != this.fa.height) !this.ea && b ? this.dispatchEvent("beginFullscreen") : this.ea && !b && this.dispatchEvent("endFullscreen"), this.fa = a, this.ea = b
    };
    h.ec = function() {
        this.V = w(this.ya, this);
        this.I = w(this.ka, this);
        this.pa = w(this.Pa, this);
        this.ca && (this.h.addEventListener("onAdStateChange", this.I), this.h.addEventListener("onReady", this.V), this.h.addEventListener("onStateChange", this.I), this.h.addEventListener("onVolumeChange", this.pa));
        this.O = new ee(1E3);
        this.L.N(this.O, "tick", this.Ff);
        this.O.start()
    };
    h.gb = function() {
        this.ca && (this.h.removeEventListener("onAdStateChange", this.I), this.h.removeEventListener("onReady", this.V), this.h.removeEventListener("onStateChange", this.I), this.h.removeEventListener("onVolumeChange", this.pa));
        null != this.O && this.O.W()
    };
    h.Od = function() {
        var a = {
                playerVars: cb(zr),
                events: {
                    onError: w(this.Yb, this),
                    onReady: w(this.ya, this),
                    onAdStateChange: w(this.ka, this),
                    onStateChange: w(this.ka, this),
                    onVolumeChange: w(this.Pa, this)
                }
            },
            b = Ca("YT");
        this.h = null != b && null != b.Player ? new b.Player(this.K, a) : null
    };
    var Ar = function(a, b, c) {
        var d = {
            autoplay: "1"
        };
        null != a.g && (null != a.g.o && (d.agcid = a.g.o), null != a.g.h && (d.adformat = a.g.h), null != a.g.l && (d.ad_query_id = a.g.l), a.g.v && (d.cta_conversion_urls = a.g.v), a.g.A && (d.endscreen_ad_tracking_data = a.g.A), a.g.B && (d.is_pharma = 1), d.iv_load_policy = a.g.H ? 1 : 3, a.g.w && (d.noiba = 1), a.g.D && (d.utpsa = 1), a.g.J && (d.autoplay = "1"));
        if (null == b) var e = null;
        else to(xp, b) ? (e = b.match(/yt_vid\/([a-zA-Z0-9_-]{11})/), e = null != e && 1 < e.length ? e[1] : null) : e = (null == b ? 0 : to(oo, b)) ? Aq(b) : null;
        null === e ? (c =
            null === c ? "" : c, b = "url=" + encodeURIComponent(b) + "&type=" + encodeURIComponent(c), d.url_encoded_third_party_media = b) : d.videoId = e;
        d.enabled_engage_types = "3,4,5,6";
        a.o = !1;
        a.g && a.g.g ? (a.ma = d, a.h.preloadVideoByPlayerVars(a.ma)) : a.h.cueVideoByPlayerVars(d);
        a.dispatchEvent("loaded")
    };
    yr.prototype.Yb = function() {
        this.dispatchEvent("error")
    };
    yr.prototype.ya = function() {
        this.l = !0; - 1 != this.w && (this.Kb(this.w), this.w = -1);
        null != this.G && (Ar(this, this.G, this.P), this.P = this.G = null); - 1 != this.F && (this.lb(this.F), this.F = -1);
        this.U && this.yb()
    };
    yr.prototype.ka = function(a) {
        switch (a.data) {
            case 0:
                this.o ? this.dispatchEvent("end") : this.dispatchEvent("error");
                break;
            case 1:
                this.o || (Br(this), this.o = !0, this.D = 0, this.dispatchEvent("start"));
                this.dispatchEvent("play");
                Cr(this);
                this.v = new ee(100);
                this.L.N(this.v, "tick", this.wa);
                this.v.start();
                break;
            case 2:
                this.dispatchEvent("pause"), Cr(this)
        }
    };
    yr.prototype.Pa = function() {
        this.dispatchEvent("volumeChange")
    };
    var Cr = function(a) {
            a.L.Na(a.v, "tick", a.wa);
            null != a.v && (a.v.stop(), a.v = null)
        },
        Br = function(a) {
            null != a.ab && n.clearTimeout(a.ab)
        };
    yr.prototype.wa = function() {
        if (le || up()) {
            if (1.5 < this.xa() - this.D) {
                this.l && this.h.seekTo(this.D, !0);
                return
            }
            this.xa() > this.D && (this.D = this.xa())
        }
        this.dispatchEvent("timeUpdate")
    };
    yr.prototype.Xb = function() {
        this.dispatchEvent("mediaLoadTimeout")
    };
    yr.prototype.T = function() {
        Cr(this);
        Br(this);
        this.gb();
        this.l = !1;
        this.L.W();
        this.F = -1;
        this.P = null;
        this.U = !1;
        this.G = null;
        this.w = -1;
        this.V = this.h = this.g = null;
        this.o = !1;
        this.ra = "";
        yr.da.T.call(this)
    };
    u("onYouTubeIframeAPIReady", function() {
        vr = !0;
        wr.forEach(function(a) {
            a()
        });
        wr = []
    }, window);
    var Er = function(a, b, c, d, e) {
        if (!(e || null != a && pd(bd(a), a))) throw Dp(Cp, null, "containerElement", "element");
        this.G = !1;
        this.J = a;
        e = null != b || null != d;
        if (!e && T.h) throw Dp(Ap, null, "Custom video element was not provided even though the setting restrictToCustomPlayback is set to true.");
        T.Y() || (T.g = 2);
        this.D = Dr(b ? b : null);
        var f = e;
        T.h || wp(this.D) && e || (f = !1);
        this.X = (this.H = f) && null != d;
        e = ld("DIV", {
            style: "position:absolute"
        });
        a.insertBefore(e, a.firstChild);
        this.B = e;
        this.g = null;
        !this.H && pp() && (this.g = new ur(this.B,
            null, !0));
        a = null;
        this.H ? b ? a = new fr(b) : d && (a = new yr(d)) : this.g && (a = new fr(this.g.h));
        this.h = a;
        this.l = this.o = null;
        a = tc && !(tc && sp(op, 4));
        e = qp() && pp();
        if (T.Y() || this.g && this.h && !this.H && T.l && !dl() && !a && !e) this.o = new ur(this.B, null, !0), this.l = new fr(this.o.h);
        this.A = this.h ? c || null : null;
        this.L = null != this.A;
        Sp(Op.C(), 8, {
            enabled: this.H,
            yt: null != d,
            customClick: null != this.A
        });
        this.H && b ? v(b.getBoundingClientRect) ? c = b : (c = this.J, T.D = c) : c = this.B;
        this.F = c;
        this.w = new tq(this.B, this);
        this.K = new E(0, 0);
        this.I = "";
        b &&
            (b = b.src || b.currentSrc, b = b instanceof Fn ? b.clone() : new Fn(b, void 0), 200 > b.toString().length ? this.I = b.toString() : 200 > b.g.length && (this.I = b.g))
    };
    Er.prototype.U = function() {
        this.G = !0;
        if (null != this.g) {
            var a = this.g;
            a.h && (a = a.h, pp() && a.load())
        }
        null != this.o && (a = this.o, a.h && (a = a.h, pp() && a.load()))
    };
    Er.prototype.P = function() {
        var a = this;
        yd(this.g);
        yd(this.o);
        yd(this.w);
        null != this.h && this.h.X(function() {
            return yd(a.h)
        });
        null != this.l && this.l.X(function() {
            return yd(a.l)
        });
        nd(this.B)
    };
    var yq = function(a) {
        return a.L && a.A ? a.A : null != a.g ? a.g.l : null
    };
    Er.prototype.v = function() {
        return this.H
    };
    Er.prototype.V = function() {
        return !1
    };
    Er.prototype.O = function() {
        return this.X
    };
    var Dr = function(a) {
        return null != a && v(a.getAttribute) && null != a.getAttribute("playsinline") ? !0 : !1
    };
    var Fr = function(a, b) {
        S.call(this, "adMetadata", a);
        this.h = b || null
    };
    y(Fr, S);
    Fr.prototype.A = function() {
        return this.h
    };
    var Gr = function(a) {
        if (a) {
            var b = /iu=\/(\d+)\//.exec(Eb(a));
            (b = b && 2 == b.length ? b[1] : null) || (a = Qb((new Fn(a)).h.get("client")), b = A(a) ? null : a);
            a = b
        } else a = null;
        return a
    };
    var Hr = function() {
            this.g = Do.C();
            var a = hp();
            A(Qb(a)) || Co(this.g, "e", a);
            Co(this.g, "alt", "0")
        },
        Ir = function(a) {
            var b = Hr.C();
            if (gh) {
                var c = Np();
                Co(b.g, "sdkv", c);
                Co(b.g, "pid", b.g.h);
                Co(b.g, "ppt", T.A);
                Co(b.g, "ppv", T.H);
                Co(b.g, "mrd", T.B);
                Co(b.g, "aab", T.l ? 1 : 0);
                Co(b.g, "itv", document.hidden ? 0 : 1);
                if (c = ih()) {
                    var d = b.g.g;
                    d.h && d.B(new jh(a, 4, c, 0, void 0))
                }
                if ("vl" == a || "ff" == a || "er" == a || "cl" == a) "0" == b.g.h ? b.g.g.v() : (a = b.g.g, a.I = !0, wo(a))
            }
        };
    Ea(Hr);
    var Jr = function(a, b, c) {
        this.h = c;
        0 == b.length && (b = [
            []
        ]);
        this.g = b.map(function(b) {
            b = a.concat(b);
            for (var c = [], d = 0, g = 0; d < b.length;) {
                var k = b[d++];
                if (128 > k) c[g++] = String.fromCharCode(k);
                else if (191 < k && 224 > k) {
                    var m = b[d++];
                    c[g++] = String.fromCharCode((k & 31) << 6 | m & 63)
                } else if (239 < k && 365 > k) {
                    m = b[d++];
                    var l = b[d++],
                        t = b[d++];
                    k = ((k & 7) << 18 | (m & 63) << 12 | (l & 63) << 6 | t & 63) - 65536;
                    c[g++] = String.fromCharCode(55296 + (k >> 10));
                    c[g++] = String.fromCharCode(56320 + (k & 1023))
                } else m = b[d++], l = b[d++], c[g++] = String.fromCharCode((k & 15) << 12 |
                    (m & 63) << 6 | l & 63)
            }
            return new RegExp(c.join(""))
        })
    };
    Jr.prototype.match = function(a) {
        var b = this;
        return this.g.some(function(c) {
            c = a.match(c);
            return null == c ? !1 : !b.h || 1 <= c.length && "3.217.0" == c[1] || 2 <= c.length && "3.217.0" == c[2] ? !0 : !1
        })
    };
    var Kr = [104, 116, 116, 112, 115, 63, 58, 47, 47, 105, 109, 97, 115, 100, 107, 92, 46, 103, 111, 111, 103, 108, 101, 97, 112, 105, 115, 92, 46, 99, 111, 109, 47, 106, 115, 47, 40, 115, 100, 107, 108, 111, 97, 100, 101, 114, 124, 99, 111, 114, 101, 41, 47],
        Lr = [104, 116, 116, 112, 115, 63, 58, 47, 47, 115, 48, 92, 46, 50, 109, 100, 110, 92, 46, 110, 101, 116, 47, 105, 110, 115, 116, 114, 101, 97, 109, 47, 104, 116, 109, 108, 53, 47],
        Mr = [104, 116, 116, 112, 115, 63, 58, 47, 47, 105, 109, 97, 115, 100, 107, 92, 46, 103, 111, 111, 103, 108, 101, 97, 112, 105, 115, 92, 46, 99, 111, 109, 47, 97, 100, 109, 111, 98, 47, 40, 115, 100,
            107, 108, 111, 97, 100, 101, 114, 124, 99, 111, 114, 101, 41, 47
        ],
        Nr = [104, 116, 116, 112, 115, 63, 58, 47, 47, 105, 109, 97, 115, 100, 107, 92, 46, 103, 111, 111, 103, 108, 101, 97, 112, 105, 115, 92, 46, 99, 111, 109, 47, 106, 115, 47, 99, 111, 114, 101, 47, 97, 100, 109, 111, 98, 47],
        Or = [104, 116, 116, 112, 115, 63, 58, 47, 47, 105, 109, 97, 115, 100, 107, 92, 46, 103, 111, 111, 103, 108, 101, 97, 112, 105, 115, 92, 46, 99, 111, 109, 47, 112, 114, 101, 114, 101, 108, 101, 97, 115, 101, 47, 106, 115, 47, 91, 48, 45, 57, 93, 43, 92, 46, 91, 48, 45, 57, 92, 46, 93, 43, 47],
        Pr = [
            [105, 109, 97, 51, 92, 46, 106, 115],
            [105, 109, 97,
                51, 95, 100, 101, 98, 117, 103, 92, 46, 106, 115
            ]
        ],
        Qr = [
            [98, 114, 105, 100, 103, 101, 40, 91, 48, 45, 57, 93, 43, 92, 46, 91, 48, 45, 57, 92, 46, 93, 43, 41, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123, 48, 44, 50, 125, 92, 46, 104, 116, 109, 108],
            [98, 114, 105, 100, 103, 101, 40, 91, 48, 45, 57, 93, 43, 92, 46, 91, 48, 45, 57, 92, 46, 93, 43, 41, 95, 100, 101, 98, 117, 103, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123, 48, 44, 50, 125, 92, 46, 104, 116, 109, 108],
            [98, 114, 105, 100, 103, 101, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123,
                48, 44, 50, 125, 92, 46, 104, 116, 109, 108
            ]
        ],
        Rr = [
            [111, 117, 116, 115, 116, 114, 101, 97, 109, 92, 46, 106, 115],
            [111, 117, 116, 115, 116, 114, 101, 97, 109, 95, 100, 101, 98, 117, 103, 92, 46, 106, 115]
        ],
        Sr = new Jr(Kr, Pr, !1),
        Tr = new Jr(Kr, Qr, !0),
        Ur = new Jr(Lr, Pr, !1),
        Vr = new Jr(Lr, Qr, !0),
        Wr = new Jr(Mr, [], !1),
        Xr = new Jr(Mr, Qr, !0),
        Yr = new Jr(Nr, Qr, !1),
        Zr = new Jr(Nr, [
            [97, 112, 112, 95, 112, 114, 111, 109, 111, 95, 105, 110, 116, 101, 114, 115, 116, 105, 116, 105, 97, 108, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123, 48, 44, 50, 125, 92, 46, 106, 115],
            [97, 112,
                112, 95, 112, 114, 111, 109, 111, 95, 105, 110, 116, 101, 114, 115, 116, 105, 116, 105, 97, 108, 95, 99, 97, 110, 97, 114, 121, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123, 48, 44, 50, 125, 92, 46, 106, 115
            ],
            [118, 105, 100, 101, 111, 95, 105, 110, 116, 101, 114, 115, 116, 105, 116, 105, 97, 108, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41, 123, 48, 44, 50, 125, 92, 46, 106, 115],
            [118, 105, 100, 101, 111, 95, 105, 110, 116, 101, 114, 115, 116, 105, 116, 105, 97, 108, 95, 99, 97, 110, 97, 114, 121, 40, 95, 40, 91, 97, 45, 122, 48, 45, 57, 93, 41, 123, 50, 44, 51, 125, 41,
                123, 48, 44, 50, 125, 92, 46, 106, 115
            ]
        ], !1),
        $r = new Jr([104, 116, 116, 112, 115, 63, 58, 47, 47, 103, 111, 111, 103, 108, 101, 97, 100, 115, 92, 46, 103, 92, 46, 100, 111, 117, 98, 108, 101, 99, 108, 105, 99, 107, 92, 46, 110, 101, 116, 47, 109, 97, 100, 115, 47, 115, 116, 97, 116, 105, 99, 47], [], !1),
        as = new Jr([104, 116, 116, 112, 115, 63, 58, 47, 47, 119, 119, 119, 92, 46, 103, 115, 116, 97, 116, 105, 99, 92, 46, 99, 111, 109, 47, 97, 100, 109, 111, 98, 47, 106, 115, 47], [], !1),
        bs = new Jr([104, 116, 116, 112, 115, 63, 58, 47, 47, 109, 105, 110, 116, 45, 109, 97, 100, 92, 46, 115, 97, 110, 100, 98, 111, 120, 92, 46,
            103, 111, 111, 103, 108, 101, 92, 46, 99, 111, 109, 47, 109, 97, 100, 115, 47, 115, 116, 97, 116, 105, 99, 47, 102, 111, 114, 109, 97, 116, 115, 47
        ], [], !1),
        cs = new Jr([104, 116, 116, 112, 115, 63, 58, 47, 47, 118, 105, 100, 101, 111, 45, 97, 100, 45, 116, 101, 115, 116, 92, 46, 97, 112, 112, 115, 112, 111, 116, 92, 46, 99, 111, 109, 47], [], !1),
        ds = new Jr(Or, Pr, !1),
        es = new Jr([104, 116, 116, 112, 115, 63, 58, 47, 47, 40, 112, 97, 103, 101, 97, 100, 50, 124, 116, 112, 99, 41, 92, 46, 103, 111, 111, 103, 108, 101, 115, 121, 110, 100, 105, 99, 97, 116, 105, 111, 110, 92, 46, 99, 111, 109, 47, 112, 97, 103, 101, 97, 100,
            47, 40, 103, 97, 100, 103, 101, 116, 115, 124, 106, 115, 41, 47
        ], [], !1),
        fs = new Jr(Kr, Rr, !1),
        gs = new Jr(Or, Rr, !1),
        Ra = {
            Vg: Sr,
            Ug: Tr,
            kh: Ur,
            jh: Vr,
            mg: Wr,
            jg: Xr,
            ig: Yr,
            kg: Zr,
            ng: $r,
            lg: as,
            hg: bs,
            og: cs,
            Wg: ds,
            di: es,
            Ch: fs,
            Dh: gs
        };
    var is = function() {
            var a = Ug(),
                b = a.h,
                c = a.g;
            a = a.l;
            var d = null;
            a && (d = hs(a.url));
            return d ? d : b && b.url ? b.url : c && c.url ? c.url : ""
        },
        hs = function(a) {
            try {
                var b = new Fn(a);
                if (!b.g.includes(".cdn.ampproject.org")) return null;
                var c = b.l.split("/").slice(1),
                    d = "s" == c[1] && 3 > c.length;
                if (2 > c.length || d) return null;
                var e = "s" == c[1];
                c = e ? c.slice(2) : c.slice(1);
                var f = decodeURIComponent(c[0]) + "/";
                return e ? "https://" + f + c.slice(1).join("/") : "http://" + f + c.slice(1).join("/")
            } catch (g) {
                return null
            }
        };
    var js = rc || sc || pc && Dc(11) || qc,
        ks = sc && "srcdoc" in document.createElement("IFRAME"),
        ls = function(a, b) {
            a.open("text/html", "replace");
            a.write(b);
            a.close()
        },
        qs = function(a, b) {
            pc && Dc(7) && !Dc(10) && 6 > ms() && ns(b) && (b = os(b));
            var c = function() {
                    var c = a.contentWindow;
                    c && (c.goog_content = b, c.location.replace("javascript:window.goog_content"))
                },
                d;
            if (d = pc) {
                try {
                    var e = Df(a.contentWindow)
                } catch (f) {
                    e = !1
                }
                d = !e
            }
            d ? ps(a, c) : c()
        },
        ms = function() {
            var a = navigator.userAgent.match(/Trident\/([0-9]+.[0-9]+)/);
            return a ? parseFloat(a[1]) :
                0
        },
        rs = 0,
        ps = function(a, b) {
            var c = "goog_rendering_callback" + rs++;
            n[c] = b;
            a.src = "javascript:'<script>(function() {document.domain = \"" + document.domain + '";var continuation = window.parent.' + c + ";window.parent." + c + " = null;continuation();})()\x3c/script>'"
        },
        ns = function(a) {
            for (var b = 0; b < a.length; ++b)
                if (127 < a.charCodeAt(b)) return !0;
            return !1
        },
        os = function(a) {
            a = unescape(encodeURIComponent(a));
            for (var b = Math.floor(a.length / 2), c = [], d = 0; d < b; ++d) c[d] = String.fromCharCode(256 * a.charCodeAt(2 * d + 1) + a.charCodeAt(2 * d));
            1 == a.length % 2 && (c[b] = a.charAt(a.length - 1));
            return c.join("")
        };
    var ts = function(a, b) {
        this.l = a;
        this.g = null;
        this.D = "";
        this.F = 0;
        this.o = this.h = null;
        this.v = b;
        this.B = null;
        this.w = ""
    };
    y(ts, G);
    ts.prototype.I = function(a) {
        try {
            var b = a.h.data;
            try {
                var c = JSON.parse(b)
            } catch (ke) {
                return
            }
            var d = c.session;
            if (null != d && this.w == d) switch (c.type) {
                case "friendlyReady":
                    var e = us(this);
                    if (pp() && null != e) {
                        this.g = e;
                        this.D = e.currentSrc;
                        this.F = e.currentTime;
                        var f = this.l;
                        null != f.g && f.g.show()
                    } else {
                        var g = this.l.J,
                            k = this.l.K;
                        var m = "border: 0; margin: 0; padding: 0; position: absolute; " + ("width:" + k.width + "px; ");
                        m += "height:" + k.height + "px;";
                        this.g = ld("VIDEO", {
                            style: m,
                            autoplay: !0
                        });
                        null != us(this) && r(us(this).volume) &&
                            (this.g.volume = us(this).volume);
                        g.appendChild(this.g)
                    }
                    var l = this.l.J;
                    a = "border: 0; margin: 0; padding: 0;position: absolute; ";
                    var t = this.g;
                    b: {
                        var H = bd(t);
                        if (H.defaultView && H.defaultView.getComputedStyle) {
                            var fa = H.defaultView.getComputedStyle(t, null);
                            if (fa) {
                                var va = fa.display || fa.getPropertyValue("display") || "";
                                break b
                            }
                        }
                        va = ""
                    }
                    if ("none" != (va || (t.currentStyle ? t.currentStyle.display : null) || t.style && t.style.display)) var La = ff(t);
                    else {
                        var V = t.style,
                            Yb = V.display,
                            Sd = V.visibility,
                            me = V.position;
                        V.visibility =
                            "hidden";
                        V.position = "absolute";
                        V.display = "inline";
                        var bc = ff(t);
                        V.display = Yb;
                        V.position = me;
                        V.visibility = Sd;
                        La = bc
                    }
                    a += "width:" + La.width + "px; ";
                    a += "height:" + La.height + "px;";
                    this.o = ld("DIV", {
                        style: a
                    });
                    l.appendChild(this.o);
                    try {
                        this.h.contentWindow.loader.initFriendly(this.g, this.o)
                    } catch (ke) {
                        vs(this)
                    }
                    $p(this.v, "vpaid", "", b);
                    break;
                case "destroyFriendlyIframe":
                    this.W();
                    break;
                case "becameLinear":
                    this.g && !Vh() && !Uh() && $e(this.g, {
                        visibility: "visible"
                    });
                    $p(this.v, "vpaid", "", b);
                    break;
                case "becameNonlinear":
                    ws(this);
                    $p(this.v, "vpaid", "", b);
                    break;
                default:
                    $p(this.v, "vpaid", "", b)
            }
        } catch (ke) {
            vs(this)
        }
    };
    var vs = function(a) {
            var b = {
                type: "error"
            };
            b.session = a.w;
            a = Ae(b);
            window.postMessage(a, "*")
        },
        us = function(a) {
            a = a.l.h;
            return a instanceof fr && a.g instanceof HTMLVideoElement ? a.g : null
        },
        ws = function(a) {
            a.g && !Vh() && !Uh() && $e(a.g, {
                visibility: "hidden"
            })
        };
    ts.prototype.T = function() {
        G.da.T.call(this);
        yd(this.G);
        this.G = null;
        nd(this.o);
        this.o = null;
        nd(this.h);
        this.h = null;
        var a = us(this);
        (Vh() || Uh()) && null != a ? (a.src = this.D, a.currentTime = this.F) : pp() && null != a ? (a.src = "", a = this.l, null != a.g && zq(a.g.g, !1)) : (nd(this.g), this.g = null)
    };
    var xs = function() {
        this.g = [];
        this.h = []
    };
    h = xs.prototype;
    h.Ga = function() {
        return this.g.length + this.h.length
    };
    h.isEmpty = function() {
        return 0 == this.g.length && 0 == this.h.length
    };
    h.clear = function() {
        this.g = [];
        this.h = []
    };
    h.contains = function(a) {
        return rb(this.g, a) || rb(this.h, a)
    };
    h.oa = function() {
        for (var a = [], b = this.g.length - 1; 0 <= b; --b) a.push(this.g[b]);
        var c = this.h.length;
        for (b = 0; b < c; ++b) a.push(this.h[b]);
        return a
    };
    var Y = function(a, b, c, d, e, f, g) {
        G.call(this);
        this.K = a;
        this.g = b;
        this.I = c;
        this.Pa = e;
        this.l = null;
        this.X = g;
        this.O = !1;
        this.G = 1;
        this.ya = d;
        this.ka = this.ca = this.V = -1;
        this.o = this.h = null;
        this.D = new xs;
        this.pa = !1;
        this.P = new Map;
        this.U = this.ma = !1;
        this.B = null;
        this.fa = f && null != this.g.A;
        this.L = w(this.Wd, this);
        this.ea = new Ep(this);
        this.ea.N(this.X, "adsManager", this.ab)
    };
    y(Y, G);
    Y.prototype.ab = function(a) {
        var b = a.ha,
            c = a.ga;
        switch (b) {
            case "error":
                ys(this);
                zs(this, c);
                break;
            case "contentPauseRequested":
                b = this.g.h;
                this.g.v() && null != this.l && this.l.restoreCustomPlaybackStateOnAdBreakComplete && null != b.Gb && b.Gb();
                this.v(a.ha, a.ga);
                break;
            case "contentResumeRequested":
                a = w(Y.prototype.v, this, b, c);
                ys(this, a);
                break;
            case "remainingTime":
                this.V = c.currentTime;
                this.ca = c.duration;
                this.ka = c.remainingTime;
                break;
            case "skip":
                this.v(b, c);
                break;
            case "log":
                a = c.adData;
                this.v(b, a, c.logData);
                break;
            case "companionBackfill":
                a = Ca("window.google_show_companion_ad");
                null != a && a();
                break;
            case "skipshown":
                this.O = !0;
                this.v(b, c);
                break;
            case "interaction":
                a = c.adData;
                this.v(b, a, c.interactionData);
                break;
            case "vpaidEvent":
                try {
                    var d = a.ga,
                        e = d.vpaidEventType;
                    if ("createFriendlyIframe" == e) {
                        var f = this.B = new ts(this.g, this.X);
                        f.w = d.session;
                        a = "about:self";
                        pc && (a = "");
                        f.h = ld("IFRAME", {
                            src: a,
                            allowtransparency: !0,
                            background: "transparent"
                        });
                        $e(f.h, {
                            display: "none",
                            width: "0",
                            height: "0"
                        });
                        var g = f.l.J;
                        g.appendChild(f.h);
                        var k =
                            g.ownerDocument,
                            m = k.defaultView || k.parentWindow;
                        null == f.B && (f.B = new Ep(f));
                        f.B.N(m, "message", f.I);
                        var l = '<body><script src="//imasdk.googleapis.com/js/sdkloader/loader.js">\x3c/script><script>' + ('loader = new VPAIDLoader(false, "' + f.w + '");') + "\x3c/script></body>";
                        if (te || je || qc) {
                            var t = f.h;
                            if (js) {
                                var H = t.contentWindow;
                                H && ls(H.document, l)
                            } else qs(t, l)
                        } else {
                            var fa = f.h;
                            if (ks) fa.srcdoc = l;
                            else if (js) {
                                var va = fa.contentWindow;
                                va && ls(va.document, l)
                            } else qs(fa, l)
                        }
                    } else "vpaidNonLinear" == e && ws(this.B)
                } catch (La) {
                    zs(this,
                        La.ga)
                }
                break;
            case "skippableStateChanged":
                a = c.adData;
                null != a.skippable && (this.O = a.skippable);
                this.v(b, c);
                break;
            case "cacheAbandonUrls":
                break;
            case "volumeChange":
                a = c.adData;
                null != a && r(a.volume) && (this.G = a.volume);
                this.v(b, c);
                break;
            default:
                this.v(b, c)
        }
    };
    Y.prototype.v = function(a, b, c) {
        if (null == b.companions) {
            var d = this.P.get(b.adId);
            b.companions = null != d ? d : []
        }
        var e = b.adData;
        this.h = d = null == e ? null : new X(e);
        switch (a) {
            case "adBreakReady":
            case "trackingUrlPinged":
            case "mediaUrlPinged":
                a = new S(a, null, b);
                break;
            case "adMetadata":
                a = null;
                null != b.adCuePoints && (a = new Eo(b.adCuePoints));
                a = new Fr(d, a);
                break;
            case "allAdsCompleted":
                this.h = null;
                this.ma = !0;
                a = new S(a, d);
                break;
            case "contentPauseRequested":
                this.U = !1;
                a = new S(a, d);
                break;
            case "contentResumeRequested":
                this.h =
                    null;
                this.U = !0;
                a = new S(a, d);
                break;
            case "loaded":
                this.V = 0;
                this.ca = d.cc();
                this.ka = d.cc();
                c = this.K;
                var f = this.L;
                b = this.Pa;
                R.C();
                c.h.set(Rq(d), f);
                c.w && c.B && (N.C().g = !0, f = c.B, N.C().A = f);
                (0 != T.g ? R.C().h : c.v) && Yq(c, "loaded", Rq(d), b);
                gp(dp(), 667080010) && null != e.gfpCookie && T.v && no() && (c = e.gfpCookie, Ge.set("__gads", c.value, c.expires, c.path, c.domain), delete e.gfpCookie);
                a = new S(a, d, e);
                break;
            case "start":
                this.P.set(b.adId, b.companions);
                null != yq(this.g) && (null == this.o ? (this.o = new Tp, this.ea.N(this.o, "click", this.Qf)) :
                    Xp(this.o), Vp(this.o, yq(this.g)));
                a = new S(a, d);
                break;
            case "complete":
                null != this.o && Xp(this.o);
                $q(this.K, this.L, Rq(d));
                this.h = null;
                this.P["delete"](b.adId);
                a = new S(a, d);
                break;
            case "log":
                e = null;
                null != c && null != c.type ? (b = c.type, b = "adLoadError" == b || "adPlayError" == b) : b = !1;
                b && (e = {
                    adError: Ho(c)
                });
                a = new S(a, d, e);
                break;
            case "interaction":
                a = new S(a, d, c);
                break;
            case "urlNavigationRequested":
                a = new S(a, d, b.urlNavigationData);
                break;
            default:
                a = new S(a, d)
        }
        this.dispatchEvent(a);
        this.ma && this.U && this.Qc()
    };
    var zs = function(a, b) {
            var c = new Io(Ho(b));
            a.pa ? (a.dispatchEvent(c), a.h && $q(a.K, a.L, Rq(a.h)), a.h = null) : a.D.h.push(c);
            a = {
                error: b.errorCode,
                vis: Ih(document)
            };
            Sp(Op.C(), 7, a, !0)
        },
        As = function(a, b, c) {
            $p(a.X, "adsManager", b, c)
        };
    Y.prototype.ra = function() {
        As(this, "contentTimeUpdate", {
            currentTime: this.w.currentTime
        })
    };
    var ys = function(a, b) {
        var c = a.g.h;
        a.g.v() && null != a.l && a.l.restoreCustomPlaybackStateOnAdBreakComplete && null != c.X ? c.X(b) : null != b && b()
    };
    h = Y.prototype;
    h.$e = function(a, b, c, d) {
        if (this.D.isEmpty()) {
            var e = this.g;
            null != d && (Sp(Op.C(), 54, {}, !0), e.D = Dr(d), T.h || wp(e.D) ? (e.H = !0, yd(e.g), yd(e.o), yd(e.l), e.g = null, e.o = null, e.l = null, yd(e.h), e.h = new fr(d), v(d.getBoundingClientRect) ? e.F = d : (e.F = e.J, T.D = e.F), null != e.w && vq(e.w, e.h)) : e.H = !1);
            this.pa = !0;
            this.Rc(a, b, c);
            As(this, "init", {
                width: a,
                height: b,
                viewMode: c
            })
        } else {
            for (; !this.D.isEmpty();) b = a = this.D, 0 == b.g.length && (b.g = b.h, b.g.reverse(), b.h = []), a = a.g.pop(), this.dispatchEvent(a);
            this.W()
        }
    };
    h.If = function() {
        return this.g.v()
    };
    h.Hf = function() {
        return this.fa
    };
    h.Ye = function() {
        return this.ka
    };
    h.Ve = function() {
        return this.O
    };
    h.Pd = function() {
        As(this, "discardAdBreak")
    };
    h.af = function() {
        As(this, "requestNextAdBreak")
    };
    h.Sc = function(a) {
        null != a && (this.l = a, As(this, "updateAdsRenderingSettings", {
            adsRenderingSettings: Bs(this)
        }))
    };
    h.Wd = function() {
        var a = null != this.h ? this.h.g.vpaid : !1,
            b = this.g.h,
            c = null != b ? b.xa() : this.V,
            d = null != b ? b.wb() : this.ca;
        return {
            currentTime: c,
            duration: d,
            isPlaying: null != b ? b.Xc() : !1,
            isVpaid: a,
            isYouTube: !1,
            volume: this.G
        }
    };
    h.bf = function() {
        As(this, "skip")
    };
    h.start = function() {
        if (this.I && !T.Y()) {
            qp() && Sp(Op.C(), 50, {
                customPlayback: this.g.v()
            });
            pp() && !this.g.G && Sp(Op.C(), 26, {
                adtagurl: this.I,
                customPlayback: this.g.v()
            });
            Mh(this.g.B) && Sp(Op.C(), 30, {
                adtagurl: this.I,
                customPlayback: this.g.v()
            });
            var a = this.g.A,
                b = this.g.B,
                c;
            if (c = a && b && !Mh(a)) a = Wq(a), b = Wq(b), c = 0 < a.width && 0 < a.height && 0 < b.width && 0 < b.height && a.left <= b.left + b.width && b.left <= a.left + a.width && a.top <= b.top + b.height && b.top <= a.top + a.height;
            c && Sp(Op.C(), 31, {
                adtagurl: this.I,
                customPlayback: this.g.v()
            })
        }
        if (pp() &&
            !this.g.G && !this.g.v()) throw Dp(Bp);
        b = this.g;
        b.L = this.fa && null != b.A;
        this.g.w.h.style.opacity = 1;
        null != this.w && 1 == this.G && (ya(this.w.muted) && this.w.muted ? this.dc(0) : r(this.w.volume) && (b = this.w.volume, 0 <= b && 1 >= b && this.dc(this.w.volume)));
        As(this, "start")
    };
    h.Qf = function() {
        if ((null == this.l || !this.l.disableClickThrough) && null != this.h) {
            var a = this.h.g.clickThroughUrl;
            null != a && (A(Qb(a)) || window.open(a, "_blank"))
        }
    };
    h.Rc = function(a, b, c) {
        var d = this.g,
            e = d.B;
        null != e && (-1 == a ? (e.style.right = "0", e.style.left = "0") : e.style.width = a + "px", -1 == b ? (e.style.bottom = "0", e.style.top = "0") : e.style.height = b + "px");
        null != d.w && (e = d.w, e.h.width = -1 == a ? "100%" : a, e.h.height = -1 == b ? "100%" : b, e.h.offsetTop = e.h.offsetTop);
        d.K = new E(a, b);
        As(this, "resize", {
            width: a,
            height: b,
            viewMode: c
        })
    };
    h.stop = function() {
        As(this, "stop")
    };
    h.Ue = function() {
        As(this, "expand")
    };
    h.Te = function() {
        As(this, "collapse")
    };
    h.Ze = function() {
        return this.G
    };
    h.dc = function(a) {
        this.G = a;
        if (!T.Y()) {
            var b = this.g.h;
            null != b && b.Kb(a)
        }
        As(this, "volume", {
            volume: a
        })
    };
    h.pause = function() {
        As(this, "pause")
    };
    h.resume = function() {
        As(this, "resume")
    };
    h.Qc = function() {
        null != this.B && (this.B.W(), this.B = null);
        this.W()
    };
    h.We = function() {
        return this.ya
    };
    h.Xe = function() {
        Sp(Op.C(), 80);
        return this.h
    };
    h.T = function() {
        As(this, "destroy");
        null != this.o && this.o.W();
        this.ea.W();
        this.D.clear();
        this.F && (this.F.stop(), this.F.W());
        $q(this.K, this.L);
        Y.da.T.call(this)
    };
    var Bs = function(a) {
        var b = {};
        null != a.l && eb(b, a.l);
        a.fa && (b.useClickElement = !1, b.disableClickThrough = !0);
        return b
    };
    Y.prototype.wa = function() {
        As(this, "click")
    };
    var Cs = function(a, b, c) {
        zd.call(this, "adsManagerLoaded");
        this.h = a;
        this.v = b;
        this.B = c || ""
    };
    y(Cs, zd);
    Cs.prototype.A = function(a, b) {
        var c = this.h;
        c.w = a;
        null != b && (c.l = b);
        null != a.currentTime && (c.F = new Jp(a), c.F.N("currentTimeUpdate", c.ra, !1, c), c.F.start(), c.ra(null));
        As(c, "configure", {
            adsRenderingSettings: Bs(c)
        });
        b && this.h.Sc(b);
        return this.h
    };
    Cs.prototype.w = function() {
        return this.v
    };
    Cs.prototype.o = function() {
        return this.B
    };
    var Ds = function(a, b, c) {
        var d = "script";
        d = void 0 === d ? "" : d;
        var e = a.createElement("link");
        try {
            e.rel = "preload";
            if (B("preload", "stylesheet")) var f = Oc(b);
            else {
                if (b instanceof Nc) var g = Oc(b);
                else {
                    if (b instanceof Rc) var k = Sc(b);
                    else {
                        if (b instanceof Rc) var m = b;
                        else b = b.Va ? b.Ha() : String(b), Tc.test(b) || (b = "about:invalid#zClosurez"), m = Uc(b);
                        k = m.Ha()
                    }
                    g = k
                }
                f = g
            }
            e.href = f
        } catch (l) {
            return
        }
        d && (e.as = d);
        c && (e.nonce = c);
        if (a = a.getElementsByTagName("head")[0]) try {
            a.appendChild(e)
        } catch (l) {}
    };
    var Es = /^\.google\.(com?\.)?[a-z]{2,3}$/,
        Fs = /\.(cn|com\.bi|do|sl|ba|by|ma|am)$/,
        Gs = n,
        Hs = function(a) {
            a = "https://" + ("adservice" + a + "/adsid/integrator.js");
            var b = ["domain=" + encodeURIComponent(n.location.hostname)];
            Ro[3] >= x() && b.push("adsid=" + encodeURIComponent(Ro[1]));
            return a + "?" + b.join("&")
        },
        Ro, Is, Qo = function() {
            Gs = n;
            Ro = Gs.googleToken = Gs.googleToken || {};
            var a = x();
            Ro[1] && Ro[3] > a && 0 < Ro[2] || (Ro[1] = "", Ro[2] = -1, Ro[3] = -1, Ro[4] = "", Ro[6] = "");
            Is = Gs.googleIMState = Gs.googleIMState || {};
            a = Is[1];
            Es.test(a) && !Fs.test(a) ||
                (Is[1] = ".google.com");
            Ga(Is[5]) || (Is[5] = []);
            ya(Is[6]) || (Is[6] = !1);
            Ga(Is[7]) || (Is[7] = []);
            r(Is[8]) || (Is[8] = 0)
        },
        Js = {
            $b: function() {
                return 0 < Is[8]
            },
            Xf: function() {
                Is[8]++
            },
            Yf: function() {
                0 < Is[8] && Is[8]--
            },
            Zf: function() {
                Is[8] = 0
            },
            Ai: function() {
                return !1
            },
            Mc: function() {
                return Is[5]
            },
            Hc: function(a) {
                try {
                    a()
                } catch (b) {
                    n.setTimeout(function() {
                        throw b;
                    }, 0)
                }
            },
            kd: function() {
                if (!Js.$b()) {
                    var a = n.document,
                        b = function(b) {
                            b = Hs(b);
                            a: {
                                try {
                                    var c = Ba();
                                    break a
                                } catch (k) {}
                                c = void 0
                            }
                            var d = c;
                            Ds(a, b, d);
                            c = a.createElement("script");
                            c.type = "text/javascript";
                            d && (c.nonce = d);
                            c.onerror = function() {
                                return n.processGoogleToken({}, 2)
                            };
                            b = Cf(b);
                            Yc(c, b);
                            try {
                                (a.head || a.body || a.documentElement).appendChild(c), Js.Xf()
                            } catch (k) {}
                        },
                        c = Is[1];
                    b(c);
                    ".google.com" != c && b(".google.com");
                    b = {};
                    var d = (b.newToken = "FBT", b);
                    n.setTimeout(function() {
                        return n.processGoogleToken(d, 1)
                    }, 1E3)
                }
            }
        },
        Ks = function(a) {
            Qo();
            var b = Gs.googleToken[5] || 0;
            a && (0 != b || Ro[3] >= x() ? Js.Hc(a) : (Js.Mc().push(a), Js.kd()));
            Ro[3] >= x() && Ro[2] >= x() || Js.kd()
        },
        Ls = function(a) {
            n.processGoogleToken =
                n.processGoogleToken || function(a, c) {
                    var b = a;
                    b = void 0 === b ? {} : b;
                    c = void 0 === c ? 0 : c;
                    a = b.newToken || "";
                    var e = "NT" == a,
                        f = parseInt(b.freshLifetimeSecs || "", 10),
                        g = parseInt(b.validLifetimeSecs || "", 10),
                        k = b["1p_jar"] || "";
                    b = b.pucrd || "";
                    Qo();
                    1 == c ? Js.Zf() : Js.Yf();
                    var m = Gs.googleToken = Gs.googleToken || {},
                        l = 0 == c && a && q(a) && !e && r(f) && 0 < f && r(g) && 0 < g && q(k);
                    e = e && !Js.$b() && (!(Ro[3] >= x()) || "NT" == Ro[1]);
                    var t = !(Ro[3] >= x()) && 0 != c;
                    if (l || e || t) e = x(), f = e + 1E3 * f, g = e + 1E3 * g, 1E-5 > Math.random() && Yf(n, "https://pagead2.googlesyndication.com/pagead/gen_204?id=imerr&err=" +
                        c, void 0), m[5] = c, m[1] = a, m[2] = f, m[3] = g, m[4] = k, m[6] = b, Qo();
                    if (l || !Js.$b()) {
                        c = Js.Mc();
                        for (a = 0; a < c.length; a++) Js.Hc(c[a]);
                        c.length = 0
                    }
                };
            Ks(a)
        };
    (function() {
        if (!Sa(function(a) {
                return a.match(F().location.href)
            })) {
            for (var a = dd(), b = null, c = null, d = 0; d < a.length; d++)
                if (c = a[d], Sa(function(a) {
                        return a.match(c.src)
                    })) {
                    b = c;
                    break
                }
            if (null == b) throw Error("IMA SDK is either not loaded from a google domain or is not a supported version.");
        }
    })();
    var Ns = function(a) {
        G.call(this);
        this.g = a;
        this.v = new Map;
        this.l = this.g.w;
        this.w = new Ep(this);
        0 != T.g ? (this.h = new Sq, xd(this, Oa(yd, this.h))) : this.h = Vq();
        if (this.l) {
            a = this.h;
            var b = rq(this.l);
            if (!a.l) {
                a.g = b || null;
                a.g && (a.F.N(a.g, "activityMonitor", a.G), ar(a));
                if (!(n.ima && n.ima.video && n.ima.video.client && n.ima.video.client.tagged)) {
                    u("ima.video.client.sdkTag", !0, void 0);
                    var c = n.document;
                    b = document.createElement("SCRIPT");
                    var d = Pc(Kc(Lc("https://s0.2mdn.net/instream/video/client.js")));
                    Yc(b, d);
                    b.async = !0;
                    b.type = "text/javascript";
                    c = c.getElementsByTagName("script")[0];
                    c.parentNode.insertBefore(b, c)
                }
                b = ip();
                Fg(b);
                R.C().K = T.g;
                T.F || (a.v = !0, R.C().h = !0);
                a.D = (v(null), null);
                T.Y() && (N.C().R = "gsv", N.C().B = 79463068);
                b = R.C();
                c = "h" == Pl(b) || "b" == Pl(b);
                d = "exc" != N.C().R;
                c && d && (b.D = !0, b.G = new wk);
                a.l = !0
            }
            this.o = Zq(this.h, this.g.F)
        }
        a: {
            try {
                var e = window.top.location.href
            } catch (f) {
                e = 2;
                break a
            }
            e = null == e ? 2 : e == window.document.location.href ? 0 : 1
        }
        Mp.l = e;
        Ms()
    };
    y(Ns, G);
    Ns.prototype.T = function() {
        this.w.W();
        var a = this.o;
        this.h.o["delete"](a);
        0 != T.g && (R.C().v[a] = null);
        Ns.da.T.call(this)
    };
    Ns.prototype.G = function() {
        this.W()
    };
    Ns.prototype.I = function(a, b) {
        var c = this;
        Ir("ar");
        T.Y() || T.lc() ? Os(this, a, b) : Ls(function() {
            So();
            To();
            Uo();
            Os(c, a, b)
        })
    };
    var Os = function(a, b, c) {
        b.adTagUrl && Sp(Op.C(), 8, {
            adtagurl: b.adTagUrl,
            customPlayback: a.g.v(),
            customClick: null != a.g.A,
            restrict: T.h
        });
        b.location = is();
        b.referrer = window.document.referrer;
        if (gp(dp(), 328840011)) {
            var d = F().location.ancestorOrigins;
            b.topOrigin = d ? 0 < d.length && 200 > d[d.length - 1].length ? d[d.length - 1] : "" : null
        }
        b.supportsYouTubeHosted = a.g.O();
        var e = b.adTagUrl,
            f = a.g.J,
            g = [],
            k = "",
            m = "";
        if (null != f) {
            for (var l = f, t = [], H = 0; l && 25 > H; ++H) {
                a: {
                    if (l && l.nodeName && l.parentElement)
                        for (var fa = l.nodeName.toString().toLowerCase(),
                                va = l.parentElement.childNodes, La = 0, V = 0; V < va.length; ++V) {
                            var Yb = va[V];
                            if (Yb.nodeName && Yb.nodeName.toString().toLowerCase() === fa) {
                                if (l === Yb) {
                                    var Sd = "." + La;
                                    break a
                                }++La
                            }
                        }
                    Sd = ""
                }
                t.push((l.nodeName && l.nodeName.toString().toLowerCase()) + "" + Sd);l = l.parentElement
            }
            k = t.join();
            if (f) {
                var me = f.ownerDocument,
                    bc = me && (me.defaultView || me.parentWindow) || null,
                    ke = [];
                if (bc) try {
                    for (var ne = bc.parent, Mm = 0; ne && ne !== bc && 25 > Mm; ++Mm) {
                        for (var Nm = ne.frames, Kf = 0; Kf < Nm.length; ++Kf)
                            if (bc === Nm[Kf]) {
                                ke.push(Kf);
                                break
                            }
                        bc = ne;
                        ne = bc.parent
                    }
                } catch (Om) {}
                m =
                    ke.join()
            } else m = ""
        }
        g.push(k, m);
        if (null != e) {
            for (var yi = 0; yi < mo.length - 1; ++yi) g.push(Ke(e, mo[yi]) || "");
            var Pm = Ke(e, "videoad_start_delay"),
                Qm = "";
            if (Pm) {
                var Rm = parseInt(Pm, 10);
                Qm = 0 > Rm ? "postroll" : 0 == Rm ? "preroll" : "midroll"
            }
            g.push(Qm)
        } else
            for (var Sm = 0; Sm < mo.length; ++Sm) g.push("");
        var Tm = g.join(":"),
            Um = Tm.length;
        if (0 == Um) var Vm = 0;
        else {
            for (var fd = 305419896, zi = 0; zi < Um; zi++) fd ^= (fd << 5) + (fd >> 2) + Tm.charCodeAt(zi) & 4294967295;
            Vm = 0 < fd ? fd : 4294967296 + fd
        }
        b.videoAdKey = Vm.toString();
        b.mediaUrl = a.g.I;
        var Wm = b.adTagUrl;
        if (null != Wm && "ca-pub-6219811747049371" != Ke(Wm, "client")) var Xm = null;
        else {
            var Ym = Ca("window.yt.util.activity.getTimeSinceActive");
            Xm = null != Ym ? Ym().toString() : null
        }
        var Zm = Xm;
        null != Zm && (b.lastActivity = Zm);
        var $m = b.adTagUrl;
        if (null == $m) var an = !1;
        else {
            var bn = new Fn($m),
                cn = bn.l;
            an = Cb(bn.g, "googleads.g.doubleclick.net") && (A(Qb(cn)) ? !1 : /\/pagead\/(live\/)?ads/.test(cn))
        }
        if (an) {
            var oe = window,
                ua = cg().document,
                Ta = {},
                Lf = cg();
            var vb = tj(cg()).aa;
            var pe = vb.location.href;
            if (vb == vb.top) var dn = {
                url: pe,
                Yc: !0
            };
            else {
                var Ai = !1,
                    Bi = vb.document;
                Bi && Bi.referrer && (pe = Bi.referrer, vb.parent == vb.top && (Ai = !0));
                var Ci = vb.location.ancestorOrigins;
                if (Ci) {
                    var Di = Ci[Ci.length - 1];
                    Di && -1 == pe.indexOf(Di) && (Ai = !1, pe = Di)
                }
                dn = {
                    url: pe,
                    Yc: Ai
                }
            }
            var Ps = dn;
            a: {
                var dc = cg(),
                    en = oe.google_ad_width || dc.google_ad_width,
                    fn = oe.google_ad_height || dc.google_ad_height;
                if (dc && dc.top == dc) var Ei = !1;
                else {
                    var Mf = ua.documentElement;
                    if (en && fn) {
                        var Nf = 1,
                            Of = 1;
                        dc.innerHeight ? (Nf = dc.innerWidth, Of = dc.innerHeight) : Mf && Mf.clientHeight ? (Nf = Mf.clientWidth, Of = Mf.clientHeight) :
                            ua.body && (Nf = ua.body.clientWidth, Of = ua.body.clientHeight);
                        if (Of > 2 * fn || Nf > 2 * en) {
                            Ei = !1;
                            break a
                        }
                    }
                    Ei = !0
                }
            }
            var ec = Ei;
            var Qs = Ps.Yc,
                Fi = cg(),
                Pf = Fi.top == Fi ? 0 : Df(Fi.top) ? 1 : 2,
                gd = 4;
            ec || 1 != Pf ? ec || 2 != Pf ? ec && 1 == Pf ? gd = 7 : ec && 2 == Pf && (gd = 8) : gd = 6 : gd = 5;
            Qs && (gd |= 16);
            var Rs = "" + gd;
            var gn = Bn();
            var Gi = !!oe.google_page_url;
            Ta.google_iframing = Rs;
            0 != gn && (Ta.google_iframing_environment = gn);
            if (!Gi && "ad.yieldmanager.com" == ua.domain) {
                for (var qe = ua.URL.substring(ua.URL.lastIndexOf("http")); - 1 < qe.indexOf("%");) try {
                    qe = decodeURIComponent(qe)
                } catch (Om) {
                    break
                }
                oe.google_page_url =
                    qe;
                Gi = !!qe
            }
            var Hi = $f(Lf);
            if (Gi) Ta.google_page_url = oe.google_page_url, Ta.google_page_location = (ec ? ua.referrer : ua.URL) || "EMPTY";
            else {
                Fj || (Fj = fh());
                var hn = Fj;
                "21061977" == (hn.g.hasOwnProperty(119) ? hn.g[119] : "") && Hi && Hi.canonicalUrl ? (Ta.google_page_url = Hi.canonicalUrl, Ta.google_page_location = (ec ? ua.referrer : ua.URL) || "EMPTY") : (ec && Df(Lf.top) && ua.referrer && Lf.top.document.referrer === ua.referrer ? Ta.google_page_url = Lf.top.document.URL : Ta.google_page_url = ec ? ua.referrer : ua.URL, Ta.google_page_location = null)
            }
            a: {
                if (ua.URL ==
                    Ta.google_page_url) try {
                    var jn = Date.parse(ua.lastModified) / 1E3;
                    break a
                } catch (Om) {}
                jn = null
            }
            Ta.google_last_modified_time = jn;
            if (vb == vb.top) var kn = vb.document.referrer;
            else {
                var ln = $f();
                kn = ln && ln.referrer || ""
            }
            Ta.google_referrer_url = kn;
            b.adSenseParams = Ta
        }
        var mn = is();
        var Ss = null != mn && 0 <= mn.indexOf("amp=1") ? !0 : null != window.context ? 0 < parseInt(window.context.ampcontextVersion, 10) : null != Ug().l;
        b.isAmp = Ss;
        var nn = "goog_" + Tb++;
        a.v.set(nn, c || null);
        var Ii = Gr(b.adTagUrl) || "",
            on = Zf(Ii);
        if (0 != on) var pn = on;
        else {
            var Ji =
                n.top;
            pn = Tf(Ji, "googlefcInactive") ? 4 : Ii && Tf(Ji, "googlefcPA-" + Ii) ? 2 : Tf(Ji, "googlefcNPA") ? 3 : 0
        }
        var qn = pn;
        var Ts = Tf(n.top, "googlefcPresent") && 4 != qn;
        var xc = {},
            L = a.B();
        eb(xc, b);
        xc.settings = {
            "1pJar": L.L,
            activeViewPushUpdates: 0 != T.g ? R.C().h : a.h.v,
            activityMonitorMode: L.g,
            adsToken: L.K,
            autoPlayAdBreaks: L.l,
            cacheAbandonUrls: !1,
            chromelessPlayer: !0,
            companionBackfill: L.J,
            cookiesEnabled: L.v,
            disableCustomPlaybackForIOS10Plus: L.o,
            engagementDetection: !0,
            isAdMob: L.Y(),
            isGdpr: L.Jf() || !1,
            isInChina: L.lc() || !1,
            isFunctionalTest: L.kc(),
            isVpaidAdapter: L.Nb(),
            numRedirects: L.B,
            onScreenDetection: !0,
            pageCorrelator: L.O,
            persistentStateCorrelator: Aj(),
            playerType: L.A,
            playerVersion: L.H,
            ppid: L.P,
            privacyControls: L.V,
            reportMediaRequests: L.$f(),
            restrictToCustomPlayback: L.h,
            streamCorrelator: L.X,
            testingConfig: Ko(L).g,
            unloadAbandonPingEnabled: L.cg(),
            urlSignals: L.fa,
            useCompanionsAsEndSlate: !1,
            useNewLogicForRewardedEndSlate: L.dg(),
            useRewardedEndSlate: L.eg(),
            useRefactoredDelayLearnMore: !1,
            vpaidMode: L.I
        };
        xc.consentSettings = {
            gfcPresent: Ts,
            gfcUserConsent: qn
        };
        var Ki;
        if (Ki = gp(dp(), 667080010)) Ki = T.v;
        if (Ki) {
            var rn = no(),
                sn = {
                    isBrowserCookieEnabled: rn
                },
                Qf;
            if (Qf = rn) {
                var tn = b.adTagUrl;
                if (null == tn) Qf = !1;
                else {
                    var un = new Fn(tn),
                        vn = un.l;
                    Qf = Cb(un.g, "doubleclick.net") && (A(Qb(vn)) ? !1 : /\/gampad\/(live\/)?ads/.test(vn))
                }
            }
            if (Qf) {
                var Us = Ge.get("__gads");
                sn.gfpCookieValue = Qb(Us)
            }
            xc.cookieSettings = sn
        }
        var wn = a.g.h;
        xc.videoEnvironment = {
            customClickTrackingProvided: null != a.g.A,
            iframeState: Mp.l,
            osdId: a.o,
            supportedMimeTypes: null != wn ? wn.Nc() : null,
            usesChromelessPlayer: a.g.V(),
            usesCustomVideoPlayback: a.g.v(),
            usesYouTubePlayer: a.g.O(),
            usesInlinePlayback: a.g.D
        };
        xc.experimentState = fp();
        var xn = rq(a.l, nn);
        a.w.N(xn, "adsLoader", a.D);
        $p(xn, "adsLoader", "requestAds", xc)
    };
    Ns.prototype.B = function() {
        return T
    };
    Ns.prototype.F = function() {
        $p(rq(this.l), "adsLoader", "contentComplete")
    };
    var Ms = function() {
        T.Y() || T.lc() || Ls(function() {
            So();
            To();
            Uo()
        })
    };
    Ns.prototype.D = function(a) {
        var b = a.ha;
        switch (b) {
            case "adsLoaded":
                b = a.ga;
                a = a.Jb;
                var c = Hr.C(),
                    d = b.adTagUrl;
                d ? (d = Gr(d) || "0", c.g.h = d) : c.g.h = "0";
                null != a && Bo(c.g.g, "rcid", a, !1);
                Ir("vl");
                c = new Y(this.h, this.g, b.adTagUrl || "", b.adCuePoints, this.o, b.isCustomClickTrackingAllowed, rq(this.l, a));
                this.dispatchEvent(new Cs(c, this.v.get(a), b.response));
                break;
            case "error":
                b = a.ga;
                a = a.Jb;
                c = Ho(b);
                this.dispatchEvent(new Io(c, this.v.get(a)));
                b = {
                    error: b.errorCode,
                    vis: Ih(document)
                };
                Sp(Op.C(), 7, b, !0);
                break;
            case "trackingUrlPinged":
                this.dispatchEvent(new S(b,
                    null, a.ga))
        }
    };
    var Z = function() {
        this.slotId = Math.floor(2147483646 * Math.random()) + 1
    };
    h = Z.prototype;
    h.clone = function() {
        var a = new Z;
        "auto" == this.videoPlayActivation ? a.setAdWillAutoPlay(!0) : "click" == this.videoPlayActivation && a.setAdWillAutoPlay(!1);
        "muted" == this.videoPlayMuted ? a.setAdWillPlayMuted(!0) : "unmuted" == this.videoPlayMuted && a.setAdWillPlayMuted(!1);
        a.adTagUrl = this.adTagUrl;
        a.o = this.o;
        a.adSenseParams = cb(this.adSenseParams);
        a.adsResponse = this.adsResponse;
        a.contentDuration = this.contentDuration;
        a.contentKeywords = this.contentKeywords ? this.contentKeywords.slice() : null;
        a.contentTitle = this.contentTitle;
        a.customMacros = cb(this.customMacros);
        a.g = this.g;
        a.location = this.location;
        a.referrer = this.referrer;
        a.A = this.A;
        a.lastActivity = this.lastActivity;
        a.language = this.language;
        a.linearAdSlotWidth = this.linearAdSlotWidth;
        a.linearAdSlotHeight = this.linearAdSlotHeight;
        a.nonLinearAdSlotWidth = this.nonLinearAdSlotWidth;
        a.nonLinearAdSlotHeight = this.nonLinearAdSlotHeight;
        a.v = this.v;
        a.videoAdKey = this.videoAdKey;
        a.tagForChildDirectedContent = this.tagForChildDirectedContent;
        a.usePostAdRequests = this.usePostAdRequests;
        a.supportsYouTubeHosted =
            this.supportsYouTubeHosted;
        a.youTubeAdType = this.youTubeAdType;
        a.youTubeVideoAdStartTime = this.youTubeVideoAdStartTime;
        a.Ic = this.Ic;
        a.Gc = this.Gc;
        a.l = this.l;
        a.h = this.h;
        a.forceNonLinearFullSlot = this.forceNonLinearFullSlot;
        a.liveStreamPrefetchSeconds = this.liveStreamPrefetchSeconds;
        a.Vc = this.Vc;
        a.Wc = this.Wc;
        a.Dc = this.Dc;
        a.Sb = this.Sb ? this.Sb.clone() : null;
        return a
    };
    h.adSenseParams = null;
    h.customMacros = null;
    h.videoPlayActivation = "unknown";
    h.videoPlayMuted = "unknown";
    h.liveStreamPrefetchSeconds = 0;
    h.linearAdSlotWidth = 0;
    h.linearAdSlotHeight = 0;
    h.nonLinearAdSlotWidth = 0;
    h.nonLinearAdSlotHeight = 0;
    h.forceNonLinearFullSlot = !1;
    h.videoAdKey = null;
    h.tagForChildDirectedContent = !1;
    h.usePostAdRequests = !1;
    h.slotId = 0;
    h.supportsYouTubeHosted = !0;
    h.youTubeVideoAdStartTime = 0;
    h.Ic = null;
    h.Gc = !1;
    h.setAdWillAutoPlay = function(a) {
        this.videoPlayActivation = a ? "auto" : "click"
    };
    h.setAdWillPlayMuted = function(a) {
        this.videoPlayMuted = a ? "muted" : "unmuted"
    };
    h.Vc = !0;
    h.Wc = !1;
    h.Dc = 5E3;
    h.Sb = null;
    X.prototype.getCompanionAds = X.prototype.xe;
    X.prototype.isLinear = X.prototype.Qe;
    X.prototype.isSkippable = X.prototype.Re;
    X.prototype.isUiDisabled = X.prototype.Se;
    X.prototype.getAdId = X.prototype.h;
    X.prototype.getAdSystem = X.prototype.ue;
    X.prototype.getAdvertiserName = X.prototype.ve;
    X.prototype.getApiFramework = X.prototype.we;
    X.prototype.getContentType = X.prototype.ye;
    X.prototype.getCreativeId = X.prototype.o;
    X.prototype.getCreativeAdId = X.prototype.l;
    X.prototype.getDescription = X.prototype.Sd;
    X.prototype.getTitle = X.prototype.Ud;
    X.prototype.getDuration = X.prototype.cc;
    X.prototype.getHeight = X.prototype.Ae;
    X.prototype.getWidth = X.prototype.Me;
    X.prototype.getVastMediaHeight = X.prototype.Ke;
    X.prototype.getVastMediaWidth = X.prototype.Le;
    X.prototype.getWrapperCreativeIds = X.prototype.Pe;
    X.prototype.getWrapperAdIds = X.prototype.Ne;
    X.prototype.getWrapperAdSystems = X.prototype.Oe;
    X.prototype.getTraffickingParameters = X.prototype.Fe;
    X.prototype.getTraffickingParametersString = X.prototype.Ge;
    X.prototype.getAdPodInfo = X.prototype.te;
    X.prototype.getUiElements = X.prototype.He;
    X.prototype.getMinSuggestedDuration = X.prototype.Ce;
    X.prototype.getMediaUrl = X.prototype.Be;
    X.prototype.getSurveyUrl = X.prototype.Ee;
    X.prototype.getSkipTimeOffset = X.prototype.De;
    X.prototype.getDealId = X.prototype.ze;
    X.prototype.getUniversalAdIdValue = X.prototype.Je;
    X.prototype.getUniversalAdIdRegistry = X.prototype.Ie;
    Eo.prototype.getCuePoints = Eo.prototype.g;
    u("google.ima.AdCuePoints.PREROLL", 0, window);
    u("google.ima.AdCuePoints.POSTROLL", -1, window);
    u("google.ima.AdDisplayContainer", Er, window);
    Er.prototype.initialize = Er.prototype.U;
    Er.prototype.destroy = Er.prototype.P;
    Qq.prototype.getPodIndex = Qq.prototype.qe;
    Qq.prototype.getTimeOffset = Qq.prototype.re;
    Qq.prototype.getTotalAds = Qq.prototype.se;
    Qq.prototype.getMaxDuration = Qq.prototype.pe;
    Qq.prototype.getAdPosition = Qq.prototype.ne;
    Qq.prototype.getIsBumper = Qq.prototype.oe;
    u("google.ima.AdError.ErrorCode.VIDEO_PLAY_ERROR", 400, window);
    u("google.ima.AdError.ErrorCode.FAILED_TO_REQUEST_ADS", 1005, window);
    u("google.ima.AdError.ErrorCode.REQUIRED_LISTENERS_NOT_ADDED", 900, window);
    u("google.ima.AdError.ErrorCode.VAST_LOAD_TIMEOUT", 301, window);
    u("google.ima.AdError.ErrorCode.VAST_NO_ADS_AFTER_WRAPPER", 303, window);
    u("google.ima.AdError.ErrorCode.VAST_MEDIA_LOAD_TIMEOUT", 402, window);
    u("google.ima.AdError.ErrorCode.VAST_TOO_MANY_REDIRECTS", 302, window);
    u("google.ima.AdError.ErrorCode.VAST_ASSET_MISMATCH", 403, window);
    u("google.ima.AdError.ErrorCode.VAST_LINEAR_ASSET_MISMATCH", 403, window);
    u("google.ima.AdError.ErrorCode.VAST_NONLINEAR_ASSET_MISMATCH", 503, window);
    u("google.ima.AdError.ErrorCode.VAST_ASSET_NOT_FOUND", 1007, window);
    u("google.ima.AdError.ErrorCode.VAST_UNSUPPORTED_VERSION", 102, window);
    u("google.ima.AdError.ErrorCode.VAST_SCHEMA_VALIDATION_ERROR", 101, window);
    u("google.ima.AdError.ErrorCode.VAST_TRAFFICKING_ERROR", 200, window);
    u("google.ima.AdError.ErrorCode.VAST_UNEXPECTED_LINEARITY", 201, window);
    u("google.ima.AdError.ErrorCode.VAST_UNEXPECTED_DURATION_ERROR", 202, window);
    u("google.ima.AdError.ErrorCode.VAST_WRAPPER_ERROR", 300, window);
    u("google.ima.AdError.ErrorCode.NONLINEAR_DIMENSIONS_ERROR", 501, window);
    u("google.ima.AdError.ErrorCode.COMPANION_REQUIRED_ERROR", 602, window);
    u("google.ima.AdError.ErrorCode.VAST_EMPTY_RESPONSE", 1009, window);
    u("google.ima.AdError.ErrorCode.UNSUPPORTED_LOCALE", 1011, window);
    u("google.ima.AdError.ErrorCode.INVALID_ARGUMENTS", 1101, window);
    u("google.ima.AdError.ErrorCode.UNKNOWN_AD_RESPONSE", 1010, window);
    u("google.ima.AdError.ErrorCode.UNKNOWN_ERROR", 900, window);
    u("google.ima.AdError.ErrorCode.OVERLAY_AD_PLAYING_FAILED", 500, window);
    u("google.ima.AdError.ErrorCode.AUTOPLAY_DISALLOWED", 1205, window);
    u("google.ima.AdError.ErrorCode.VIDEO_ELEMENT_USED", -1, window);
    u("google.ima.AdError.ErrorCode.VIDEO_ELEMENT_REQUIRED", -1, window);
    u("google.ima.AdError.ErrorCode.VAST_MEDIA_ERROR", -1, window);
    u("google.ima.AdError.ErrorCode.ADSLOT_NOT_VISIBLE", -1, window);
    u("google.ima.AdError.ErrorCode.OVERLAY_AD_LOADING_FAILED", -1, window);
    u("google.ima.AdError.ErrorCode.VAST_MALFORMED_RESPONSE", -1, window);
    u("google.ima.AdError.ErrorCode.COMPANION_AD_LOADING_FAILED", -1, window);
    u("google.ima.AdError.Type.AD_LOAD", "adLoadError", window);
    u("google.ima.AdError.Type.AD_PLAY", "adPlayError", window);
    Go.prototype.getErrorCode = Go.prototype.he;
    Go.prototype.getVastErrorCode = Go.prototype.Vd;
    Go.prototype.getInnerError = Go.prototype.ie;
    Go.prototype.getMessage = Go.prototype.je;
    Go.prototype.getType = Go.prototype.ke;
    u("google.ima.AdErrorEvent.Type.AD_ERROR", "adError", window);
    Io.prototype.getError = Io.prototype.v;
    Io.prototype.getUserRequestContext = Io.prototype.A;
    u("google.ima.AdEvent.Type.CONTENT_RESUME_REQUESTED", "contentResumeRequested", window);
    u("google.ima.AdEvent.Type.CONTENT_PAUSE_REQUESTED", "contentPauseRequested", window);
    u("google.ima.AdEvent.Type.CLICK", "click", window);
    u("google.ima.AdEvent.Type.DURATION_CHANGE", "durationChange", window);
    u("google.ima.AdEvent.Type.EXPANDED_CHANGED", "expandedChanged", window);
    u("google.ima.AdEvent.Type.STARTED", "start", window);
    u("google.ima.AdEvent.Type.IMPRESSION", "impression", window);
    u("google.ima.AdEvent.Type.PAUSED", "pause", window);
    u("google.ima.AdEvent.Type.RESUMED", "resume", window);
    u("google.ima.AdEvent.Type.FIRST_QUARTILE", "firstquartile", window);
    u("google.ima.AdEvent.Type.MIDPOINT", "midpoint", window);
    u("google.ima.AdEvent.Type.THIRD_QUARTILE", "thirdquartile", window);
    u("google.ima.AdEvent.Type.COMPLETE", "complete", window);
    u("google.ima.AdEvent.Type.USER_CLOSE", "userClose", window);
    u("google.ima.AdEvent.Type.LINEAR_CHANGED", "linearChanged", window);
    u("google.ima.AdEvent.Type.LOADED", "loaded", window);
    u("google.ima.AdEvent.Type.AD_CAN_PLAY", "adCanPlay", window);
    u("google.ima.AdEvent.Type.AD_METADATA", "adMetadata", window);
    u("google.ima.AdEvent.Type.AD_BREAK_READY", "adBreakReady", window);
    u("google.ima.AdEvent.Type.INTERACTION", "interaction", window);
    u("google.ima.AdEvent.Type.ALL_ADS_COMPLETED", "allAdsCompleted", window);
    u("google.ima.AdEvent.Type.SKIPPED", "skip", window);
    u("google.ima.AdEvent.Type.SKIPPABLE_STATE_CHANGED", "skippableStateChanged", window);
    u("google.ima.AdEvent.Type.LOG", "log", window);
    u("google.ima.AdEvent.Type.VIEWABLE_IMPRESSION", "viewable_impression", window);
    u("google.ima.AdEvent.Type.VOLUME_CHANGED", "volumeChange", window);
    u("google.ima.AdEvent.Type.VOLUME_MUTED", "mute", window);
    S.prototype.type = S.prototype.type;
    S.prototype.getAd = S.prototype.w;
    S.prototype.getAdData = S.prototype.B;
    Fr.prototype.getAdCuePoints = Fr.prototype.A;
    u("google.ima.AdsLoader", Ns, window);
    Ns.prototype.getSettings = Ns.prototype.B;
    Ns.prototype.requestAds = Ns.prototype.I;
    Ns.prototype.contentComplete = Ns.prototype.F;
    Ns.prototype.destroy = Ns.prototype.G;
    u("google.ima.AdsManagerLoadedEvent.Type.ADS_MANAGER_LOADED", "adsManagerLoaded", window);
    Cs.prototype.getAdsManager = Cs.prototype.A;
    Cs.prototype.getUserRequestContext = Cs.prototype.w;
    Cs.prototype.getResponse = Cs.prototype.o;
    u("google.ima.CompanionAdSelectionSettings", Bq, window);
    u("google.ima.CompanionAdSelectionSettings.CreativeType.IMAGE", "Image", void 0);
    u("google.ima.CompanionAdSelectionSettings.CreativeType.FLASH", "Flash", void 0);
    u("google.ima.CompanionAdSelectionSettings.CreativeType.ALL", "All", void 0);
    u("google.ima.CompanionAdSelectionSettings.ResourceType.HTML", "Html", void 0);
    u("google.ima.CompanionAdSelectionSettings.ResourceType.IFRAME", "IFrame", void 0);
    u("google.ima.CompanionAdSelectionSettings.ResourceType.STATIC", "Static", void 0);
    u("google.ima.CompanionAdSelectionSettings.ResourceType.ALL", "All", void 0);
    u("google.ima.CompanionAdSelectionSettings.SizeCriteria.IGNORE", "IgnoreSize", void 0);
    u("google.ima.CompanionAdSelectionSettings.SizeCriteria.SELECT_EXACT_MATCH", "SelectExactMatch", void 0);
    u("google.ima.CompanionAdSelectionSettings.SizeCriteria.SELECT_NEAR_MATCH", "SelectNearMatch", void 0);
    u("google.ima.CustomContentLoadedEvent.Type.CUSTOM_CONTENT_LOADED", "deprecated-event", window);
    u("ima.ImaSdkSettings", U, window);
    u("google.ima.settings", T, window);
    U.prototype.setCompanionBackfill = U.prototype.pf;
    U.prototype.getCompanionBackfill = U.prototype.cf;
    U.prototype.setAutoPlayAdBreaks = U.prototype.nf;
    U.prototype.isAutoPlayAdBreak = U.prototype.lf;
    U.prototype.setPpid = U.prototype.zf;
    U.prototype.getPpid = U.prototype.kf;
    U.prototype.setVpaidAllowed = U.prototype.Bf;
    U.prototype.setVpaidMode = U.prototype.Cf;
    U.prototype.setIsVpaidAdapter = U.prototype.uf;
    U.prototype.isVpaidAdapter = U.prototype.Nb;
    U.prototype.setRestrictToCustomPlayback = U.prototype.Af;
    U.prototype.isRestrictToCustomPlayback = U.prototype.Kf;
    U.prototype.setNumRedirects = U.prototype.wf;
    U.prototype.getNumRedirects = U.prototype.gf;
    U.prototype.getLocale = U.prototype.Td;
    U.prototype.setLocale = U.prototype.vf;
    U.prototype.getPlayerType = U.prototype.hf;
    U.prototype.setPlayerType = U.prototype.xf;
    U.prototype.getDisableFlashAds = U.prototype.ff;
    U.prototype.setDisableFlashAds = U.prototype.sf;
    U.prototype.getPlayerVersion = U.prototype.jf;
    U.prototype.setPlayerVersion = U.prototype.yf;
    U.prototype.setPageCorrelator = U.prototype.ca;
    U.prototype.setStreamCorrelator = U.prototype.ea;
    U.prototype.setIsOutstreamVideo = U.prototype.tf;
    U.prototype.isOutstreamVideo = U.prototype.mf;
    U.prototype.setDisableCustomPlaybackForIOS10Plus = U.prototype.rf;
    U.prototype.getDisableCustomPlaybackForIOS10Plus = U.prototype.df;
    U.prototype.setCookiesEnabled = U.prototype.qf;
    u("google.ima.ImaSdkSettings.CompanionBackfillMode.ALWAYS", "always", void 0);
    u("google.ima.ImaSdkSettings.CompanionBackfillMode.ON_MASTER_AD", "on_master_ad", void 0);
    u("google.ima.ImaSdkSettings.VpaidMode.DISABLED", 0, void 0);
    u("google.ima.ImaSdkSettings.VpaidMode.ENABLED", 1, void 0);
    u("google.ima.ImaSdkSettings.VpaidMode.INSECURE", 2, void 0);
    u("google.ima.common.adTrackingMonitor", cr, window);
    Sq.prototype.setActiveViewUseOsdGeometry = Sq.prototype.K;
    Sq.prototype.getActiveViewUseOsdGeometry = Sq.prototype.I;
    Sq.prototype.setBlockId = Sq.prototype.L;
    u("google.ima.AdsRenderingSettings", Kp, window);
    u("google.ima.AdsRenderingSettings.AUTO_SCALE", -1, window);
    u("google.ima.AdsRequest", Z, window);
    Z.prototype.adTagUrl = Z.prototype.adTagUrl;
    Z.prototype.adsResponse = Z.prototype.adsResponse;
    Z.prototype.nonLinearAdSlotHeight = Z.prototype.nonLinearAdSlotHeight;
    Z.prototype.nonLinearAdSlotWidth = Z.prototype.nonLinearAdSlotWidth;
    Z.prototype.linearAdSlotHeight = Z.prototype.linearAdSlotHeight;
    Z.prototype.linearAdSlotWidth = Z.prototype.linearAdSlotWidth;
    Z.prototype.setAdWillAutoPlay = Z.prototype.setAdWillAutoPlay;
    Z.prototype.setAdWillPlayMuted = Z.prototype.setAdWillPlayMuted;
    Z.prototype.contentDuration = Z.prototype.contentDuration;
    Z.prototype.contentKeywords = Z.prototype.contentKeywords;
    Z.prototype.contentTitle = Z.prototype.contentTitle;
    Z.prototype.vastLoadTimeout = Z.prototype.Dc;
    u("google.ima.VERSION", "3.217.0", void 0);
    u("google.ima.UiElements.AD_ATTRIBUTION", "adAttribution", void 0);
    u("google.ima.UiElements.COUNTDOWN", "countdown", void 0);
    u("google.ima.ViewMode.NORMAL", "normal", void 0);
    u("google.ima.ViewMode.FULLSCREEN", "fullscreen", void 0);
    Y.prototype.isCustomPlaybackUsed = Y.prototype.If;
    Y.prototype.isCustomClickTrackingUsed = Y.prototype.Hf;
    Y.prototype.destroy = Y.prototype.Qc;
    Y.prototype.init = Y.prototype.$e;
    Y.prototype.start = Y.prototype.start;
    Y.prototype.stop = Y.prototype.stop;
    Y.prototype.pause = Y.prototype.pause;
    Y.prototype.resume = Y.prototype.resume;
    Y.prototype.getCuePoints = Y.prototype.We;
    Y.prototype.getCurrentAd = Y.prototype.Xe;
    Y.prototype.getRemainingTime = Y.prototype.Ye;
    Y.prototype.expand = Y.prototype.Ue;
    Y.prototype.collapse = Y.prototype.Te;
    Y.prototype.getAdSkippableState = Y.prototype.Ve;
    Y.prototype.resize = Y.prototype.Rc;
    Y.prototype.skip = Y.prototype.bf;
    Y.prototype.getVolume = Y.prototype.Ze;
    Y.prototype.setVolume = Y.prototype.dc;
    Y.prototype.discardAdBreak = Y.prototype.Pd;
    Y.prototype.requestNextAdBreak = Y.prototype.af;
    Y.prototype.updateAdsRenderingSettings = Y.prototype.Sc;
    Y.prototype.clicked = Y.prototype.wa;
    Pq.prototype.getContent = Pq.prototype.getContent;
    Pq.prototype.getContentType = Pq.prototype.w;
    Pq.prototype.getHeight = Pq.prototype.B;
    Pq.prototype.getWidth = Pq.prototype.H;
})();
