//
//  MainViewController.m
//  GrocerList
//
//  Created by Esau Rubio on 11/5/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import "MainViewController.h"
#import "CRToast.h"
#import "Reachability.h"
#import <Parse/Parse.h>

@interface MainViewController ()

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    path = [rootPath stringByAppendingPathComponent:@"Data.plist"];
    
    [self onRefreshData];
    [NSTimer scheduledTimerWithTimeInterval:30.0 target:self selector:@selector(onRefreshData) userInfo:nil repeats:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)onRefreshData {
    if ([self onCheckConnection]) {
        PFQuery *query = [PFQuery queryWithClassName:@"Groceries"];
        query.cachePolicy = kPFCachePolicyNetworkElseCache;
        [query findObjectsInBackgroundWithBlock:^(NSArray *objects, NSError *error) {
            if (error == nil) {
                [self onTableViewData:objects];
            } else {
                NSDictionary *options = @{
                                          kCRToastTextKey : [error userInfo],
                                          kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                          kCRToastBackgroundColorKey : [UIColor redColor],
                                          kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                          kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                          kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationInTimeIntervalKey : @0.25,
                                          kCRToastTimeIntervalKey : @0.75
                                          };
                
                [CRToastManager showNotificationWithOptions:options completionBlock:nil];
            }
        }];
    } else {
        PFQuery *query = [PFQuery queryWithClassName:@"Groceries"];
        query.cachePolicy = kPFCachePolicyCacheOnly;
        [query findObjectsInBackgroundWithBlock:^(NSArray *objects, NSError *error) {
            if (error == nil) {
                [self onTableViewData:objects];
            } else {
                NSDictionary *options = @{
                                          kCRToastTextKey : [error userInfo],
                                          kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                          kCRToastBackgroundColorKey : [UIColor redColor],
                                          kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                          kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                          kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationInTimeIntervalKey : @0.25,
                                          kCRToastTimeIntervalKey : @0.75
                                          };
                
                [CRToastManager showNotificationWithOptions:options completionBlock:nil];
            }
        }];
    }
}

-(IBAction)onClick:(id)sender {
    UIButton *button = (UIButton*)sender;
    
    if (button.tag == 0) {
        UIAlertController *alertController = [UIAlertController
                                              alertControllerWithTitle:@"Are you Sure?"
                                              message:nil
                                              preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction
                                       actionWithTitle:@"Cancel"
                                       style:UIAlertActionStyleCancel
                                       handler:^(UIAlertAction *action) {
                                           NSLog(@"Cancel");
                                       }];
        
        UIAlertAction *logoutAction = [UIAlertAction
                                       actionWithTitle:@"Log Out"
                                       style:UIAlertActionStyleDestructive
                                       handler:^(UIAlertAction *action) {
                                           [PFUser logOut];
                                           [self performSegueWithIdentifier:@"loggedOut" sender:self];
                                       }];
        
        [alertController addAction:logoutAction];
        [alertController addAction:cancelAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    } else if (button.tag == 1) {
        [self onAddAlertView:true object:nil];
    }
}

- (void)onAddAlertView:(BOOL)adding object:(PFObject*)object {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"Add Grocery" message:nil preferredStyle:UIAlertControllerStyleAlert];
    
    [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
        textField.placeholder = @"Grocery";
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(checkAlertTextFields:) name:UITextFieldTextDidChangeNotification object:textField];
    }];
    
    [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
        textField.placeholder = @"#";
        textField.keyboardType = UIKeyboardTypeNumberPad;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(checkAlertTextFields:) name:UITextFieldTextDidChangeNotification object:textField];
    }];
    
    UIAlertAction *addAction;
    UIAlertAction *cancelAction = [UIAlertAction
                                   actionWithTitle:@"Cancel"
                                   style:UIAlertActionStyleCancel
                                   handler:^(UIAlertAction *action) {
                                       NSLog(@"Cancel");
                                   }];
    
    UITextField *grocery = alertController.textFields.firstObject;
    UITextField *rawNumber = alertController.textFields.lastObject;
    
    if (adding) {
        //
        // Grocery Add Logic
        //
        addAction = [UIAlertAction actionWithTitle:@"Add" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSNumber *number = @([rawNumber.text intValue]);
            
            PFObject *gameScore = [PFObject objectWithClassName:@"Groceries"];
            gameScore[@"grocery"] = grocery.text;
            gameScore[@"number"] = number;
            gameScore.ACL = [PFACL ACLWithUser:[PFUser currentUser]];
            
            if ([self onCheckConnection]) {
                [gameScore saveInBackgroundWithBlock: ^(BOOL succeeded, NSError *error) {
                    if (succeeded) {
                        [self onRefreshData];
                    } else {
                        NSDictionary *options = @{
                                                  kCRToastTextKey : [error userInfo],
                                                  kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                                  kCRToastBackgroundColorKey : [UIColor redColor],
                                                  kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                                  kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                                  kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationInTimeIntervalKey : @0.25,
                                                  kCRToastTimeIntervalKey : @0.75
                                                  };
                        
                        [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                    }
                }];
            } else {
                NSDictionary *options = @{
                                          kCRToastTextKey : @"Pending Save",
                                          kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                          kCRToastBackgroundColorKey : [UIColor blueColor],
                                          kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                          kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                          kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationInTimeIntervalKey : @0.25,
                                          kCRToastTimeIntervalKey : @0.75
                                          };
                
                [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                [data addObject:gameScore];
                [groceriesTable reloadData];
                
                [gameScore saveEventually:^(BOOL succeeded, NSError *error) {
                    if (succeeded) {
                        [self onRefreshData];
                    } else {
                        NSDictionary *options = @{
                                                  kCRToastTextKey : [error userInfo],
                                                  kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                                  kCRToastBackgroundColorKey : [UIColor redColor],
                                                  kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                                  kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                                  kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationInTimeIntervalKey : @0.25,
                                                  kCRToastTimeIntervalKey : @0.75
                                                  };
                        
                        [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                    }
                }];
            }
        }];
    } else {
        //
        // Grocery Update Logic
        //
        [grocery setText:[object objectForKey:@"grocery"]];
        [rawNumber setText:[NSString stringWithFormat:@"%@", [object objectForKey:@"number"]]];
        
        addAction = [UIAlertAction actionWithTitle:@"Update" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSNumber *number = @([rawNumber.text intValue]);
            
            object[@"grocery"] = grocery.text;
            object[@"number"] = number;
            
            if ([self onCheckConnection]) {
                [object saveInBackgroundWithBlock: ^(BOOL succeeded, NSError *error) {
                    if (succeeded) {
                        [self onRefreshData];
                    } else {
                        NSDictionary *options = @{
                                                  kCRToastTextKey : [error userInfo],
                                                  kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                                  kCRToastBackgroundColorKey : [UIColor redColor],
                                                  kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                                  kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                                  kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationInTimeIntervalKey : @0.25,
                                                  kCRToastTimeIntervalKey : @0.75
                                                  };
                        
                        [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                    }
                }];
            } else {
                NSDictionary *options = @{
                                          kCRToastTextKey : @"Pending Update",
                                          kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                          kCRToastBackgroundColorKey : [UIColor blueColor],
                                          kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                          kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                          kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                          kCRToastAnimationInTimeIntervalKey : @0.25,
                                          kCRToastTimeIntervalKey : @0.75
                                          };
                
                [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                
                [object saveEventually: ^(BOOL succeeded, NSError *error) {
                    if (succeeded) {
                        [self onRefreshData];
                    } else {
                        NSDictionary *options = @{
                                                  kCRToastTextKey : [error userInfo],
                                                  kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                                  kCRToastBackgroundColorKey : [UIColor redColor],
                                                  kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                                  kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                                  kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                                  kCRToastAnimationInTimeIntervalKey : @0.25,
                                                  kCRToastTimeIntervalKey : @0.75
                                                  };
                        
                        [CRToastManager showNotificationWithOptions:options completionBlock:nil];
                    }
                }];
            }
        }];
    }
    
    [alertController addAction:cancelAction];
    [alertController addAction:addAction];
    addAction.enabled = false;
    
    [self presentViewController:alertController animated:YES completion:nil];
}

// Check the user input

- (void)checkAlertTextFields:(NSNotification *)notification {
    UIAlertController *alertController = (UIAlertController *)self.presentedViewController;
    if (alertController)
    {
        UITextField *grocery = alertController.textFields.firstObject;
        UITextField *number = alertController.textFields.lastObject;
        UIAlertAction *okAction = alertController.actions.lastObject;
        NSScanner *scan = [NSScanner scannerWithString:number.text];
        
        if (![scan scanInt:nil]) return;
        
        okAction.enabled = (grocery.text.length > 2 && number.text.length > 0);
    }
}

-(BOOL)onCheckConnection {
    Reachability *check = [Reachability reachabilityForInternetConnection];
    NetworkStatus netStatus = [check currentReachabilityStatus];
    if (netStatus == NotReachable) {
        return false;
    } else {
        return true;
    }
}

#pragma Table VIew
//--------------------

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    return UITableViewCellEditingStyleDelete;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (data == nil) {
        return 0;
    } else {
        return data.count;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (data == nil || data.count == 0) {
        return nil;
    } else {
        UITableViewCell *simpleCell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"Cell"];
        if (simpleCell != nil) {
            PFObject *temp = data [indexPath.row];
            simpleCell.textLabel.text = temp[@"grocery"];
            simpleCell.detailTextLabel.text = [NSString stringWithFormat:@"%d", [[temp objectForKey:@"number"] intValue]] ;
        }
        
        return simpleCell;
    }
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        if ([self onCheckConnection]) {
            [data[indexPath.row] deleteInBackground];
        } else {
            NSDictionary *options = @{
                                      kCRToastTextKey : @"Pending Delete",
                                      kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                      kCRToastTextColorKey : [UIColor blackColor],
                                      kCRToastBackgroundColorKey : [UIColor orangeColor],
                                      kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                                      kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                                      kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                                      kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                                      kCRToastAnimationInTimeIntervalKey : @0.25,
                                      kCRToastTimeIntervalKey : @0.75
                                      };
            
            [CRToastManager showNotificationWithOptions:options completionBlock:nil];
            
            [data[indexPath.row] deleteEventually];
        }
        [data removeObjectAtIndex:indexPath.row];
        [groceriesTable deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:true];
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    PFObject *toSend = [data objectAtIndex:indexPath.row];
    [self onAddAlertView:false object:toSend];
}

- (void)onTableViewData:(NSArray*)incoming {
    data = [NSMutableArray arrayWithArray:incoming];
    [groceriesTable reloadData];
}

@end
