var gulp = require('gulp'),
    ts = require('gulp-typescript'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    cleanCSS  = require('gulp-clean-css'),
    rename = require("gulp-rename"),
    templateCache = require('gulp-angular-templatecache'),
    htmlmin = require('gulp-htmlmin'),
    runSequence = require('run-sequence');

var appSourcesRoot = 'src/main/resources/static';
var appJsDir = appSourcesRoot + '/app';
var appStylesDir = appSourcesRoot + '/styles';

var buildDir = gutil.env.buildtype === 'prod' ? 'build/resources/main/static' : 'out/production/resources/static';
var buildCodeDir = buildDir + '/app';
var buildCssDir = buildDir + '/css';

gulp.task('buildAndWatch', function(callback) {
    runSequence('build', 'watch', callback);
});

// Task to watch resource changes. If a resource changes, the build is triggered
gulp.task('watch', function() {
    gulp.watch(appSourcesRoot + '/**/*.*', ['build']);
});

gulp.task('build', function(callback) {
    runSequence(
                'build-ts',
                'build-templatecache',
                'build-css',
        callback);
});

gulp.task('build-ts', function () {
    return gulp.src(appJsDir + '/**/*.ts')
        .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.init() : gutil.noop())
        .pipe(ts({
            outFile: 'home-min.js',
            diagnostics: true
        }))
        .pipe(uglify())
        .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.write() : gutil.noop())
        .pipe(gulp.dest(buildCodeDir));
});

gulp.task('build-templatecache', function () {
    return gulp.src(appSourcesRoot + '/**/*.html')
        .pipe(htmlmin({
                        collapseWhitespace: true,
                        removeComments: true,
                        collapseBooleanAttributes: true,
                        removeAttributeQuotes: true,
                        removeRedundantAttributes: true,
                        removeEmptyAttributes: true
                       }))
        .pipe(templateCache('templatecache.js'))
        .pipe(gulp.dest(buildDir + '/generated'));
});

gulp.task('build-css', function() {
    return gulp.src(appStylesDir + '/main.less')
        .pipe(less())
        .pipe(cleanCSS())
        .pipe(rename('home.css'))
        .pipe(gulp.dest(buildCssDir));
});